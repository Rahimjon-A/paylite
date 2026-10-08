package com.paylite.service.impl;

import com.paylite.client.CardBankClient;
import com.paylite.domain.P2POperation;
import com.paylite.domain.dto.CardInfo;
import com.paylite.domain.dto.CardResponse;
import com.paylite.domain.dto.P2PRequest;
import com.paylite.domain.dto.P2PResponse;
import com.paylite.domain.enumeration.AgentCardStatus;
import com.paylite.domain.enumeration.AgentCardType;
import com.paylite.domain.enumeration.P2POperationStatus;
import com.paylite.repository.P2PRepository;
import com.paylite.service.P2PService;
import com.paylite.service.card.CardNetworkClient;
import com.paylite.service.card.CardNetworkClientFactory;
import com.paylite.web.rest.errors.P2PCardException;
import com.paylite.web.rest.errors.P2PInsufficientBalanceException;
import java.time.Instant;
import org.springframework.stereotype.Service;

@Service
public class P2PServiceImpl implements P2PService {

    private final P2PRepository operationRepository;
    private final CardBankClient cardBankClient;
    private final CardValidationService cardValidationService;
    private final CommissionService commissionService;
    private final CardNetworkClientFactory networkClientFactory;

    public P2PServiceImpl(
        P2PRepository operationRepository,
        CardBankClient cardBankClient,
        CardValidationService cardValidationService,
        CommissionService commissionService,
        CardNetworkClientFactory networkClientFactory
    ) {
        this.operationRepository = operationRepository;
        this.cardBankClient = cardBankClient;
        this.cardValidationService = cardValidationService;
        this.commissionService = commissionService;
        this.networkClientFactory = networkClientFactory;
    }

    public P2PResponse execute(P2PRequest request) {
        /*
         * Idempotency check.
         */
        var existing = operationRepository.findByRequestId(request.requestId());

        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        /*
         * 1. Get card information.
         */
        CardInfo fromCard = getCard(request.fromPan());
        CardInfo toCard = getCard(request.toPan());

        /*
         * 2. Validate cards.
         */
        validateCards(fromCard, toCard);

        /*
         * 3. Calculate commission.
         */
        long commission = commissionService.calculate(fromCard.type(), toCard.type(), request.amount());

        long totalAmount = Math.addExact(request.amount(), commission);

        /*
         * 4. Create operation record.
         */
        P2POperation operation = createOperation(request, fromCard, toCard, commission, totalAmount);

        operation.setStatus(P2POperationStatus.BALANCE_CHECKED);
        operation.setUpdatedAt(Instant.now());

        operationRepository.save(operation);

        /*
         * 5. Get sender network.
         */
        CardNetworkClient senderClient = networkClientFactory.get(fromCard.type());

        CardNetworkClient receiverClient = networkClientFactory.get(toCard.type());

        /*
         * 6. Check sender balance.
         */
        checkBalance(senderClient, fromCard.pan(), totalAmount);

        /*
         * 7. Withdraw.
         */
        try {
            senderClient.withdraw(fromCard.pan(), totalAmount);

            operation.setStatus(P2POperationStatus.WITHDRAWN);
            operation.setUpdatedAt(Instant.now());
            operationRepository.save(operation);
        } catch (Exception exception) {
            operation.setStatus(P2POperationStatus.FAILED);
            operation.setFailureReason("Unable to withdraw sender funds");
            operation.setUpdatedAt(Instant.now());
            operationRepository.save(operation);

            throw new P2PCardException("Unable to withdraw sender funds", exception);
        }

        /*
         * 8. Pay recipient.
         */
        operation.setStatus(P2POperationStatus.PAYING);
        operation.setUpdatedAt(Instant.now());
        operationRepository.save(operation);

        try {
            receiverClient.deposit(toCard.pan(), request.amount());

            operation.setStatus(P2POperationStatus.COMPLETED);
            operation.setUpdatedAt(Instant.now());
            operationRepository.save(operation);

            return toResponse(operation);
        } catch (Exception paymentException) {
            /*
             * 9. Payment failed.
             * Start compensation.
             */
            operation.setStatus(P2POperationStatus.COMPENSATING);
            operation.setFailureReason("Recipient payment failed");
            operation.setUpdatedAt(Instant.now());
            operationRepository.save(operation);

            return compensate(operation, senderClient, fromCard.pan(), totalAmount, paymentException);
        }
    }

    private CardInfo getCard(String pan) {
        try {
            CardResponse response = cardBankClient.getCardByPan(pan);
            return toCardInfo(response);
        } catch (Exception exception) {
            throw new P2PCardException("Unable to get card information for PAN: " + pan, exception);
        }
    }

    private void validateCards(CardInfo fromCard, CardInfo toCard) {
        cardValidationService.validate(fromCard);
        cardValidationService.validate(toCard);

        if (fromCard.pan().equals(toCard.pan())) {
            throw new P2PCardException("Sender and recipient cards must be different");
        }
    }

    private void checkBalance(CardNetworkClient senderClient, String pan, long totalAmount) {
        Long balance = senderClient.getBalance(pan);

        if (balance < totalAmount) {
            throw new P2PInsufficientBalanceException("Insufficient balance");
        }
    }

    private P2PResponse compensate(
        P2POperation operation,
        CardNetworkClient senderClient,
        String senderPan,
        long amount,
        Exception paymentException
    ) {
        try {
            senderClient.deposit(senderPan, amount);

            operation.setStatus(P2POperationStatus.COMPENSATED);
            operation.setFailureReason("Recipient payment failed; funds returned");
            operation.setUpdatedAt(Instant.now());

            operationRepository.save(operation);

            return toResponse(operation);
        } catch (Exception compensationException) {
            operation.setStatus(P2POperationStatus.COMPENSATION_FAILED);
            operation.setFailureReason("Payment failed and compensation failed");
            operation.setUpdatedAt(Instant.now());

            operationRepository.save(operation);

            throw new P2PCardException("Payment failed and compensation failed", compensationException);
        }
    }

    private P2POperation createOperation(P2PRequest request, CardInfo fromCard, CardInfo toCard, long commission, long totalAmount) {
        Instant now = Instant.now();

        return new P2POperation()
            .requestId(request.requestId())
            .amount(request.amount())
            .commissionAmount(commission)
            .totalAmount(totalAmount)
            .fromPan(fromCard.pan())
            .fromType(fromCard.type())
            .fromExpireDate(fromCard.expireDate())
            .toPan(toCard.pan())
            .toType(toCard.type())
            .toExpireDate(toCard.expireDate())
            .status(P2POperationStatus.CREATED)
            .createdAt(now)
            .updatedAt(now);
    }

    private CardInfo toCardInfo(CardResponse response) {
        return new CardInfo(
            response.pan(),
            AgentCardType.valueOf(response.type()),
            response.expireDate(),
            AgentCardStatus.valueOf(response.status())
        );
    }

    private P2PResponse toResponse(P2POperation operation) {
        return new P2PResponse(
            operation.getRequestId(),
            operation.getAmount(),
            operation.getCommissionAmount(),
            operation.getTotalAmount(),
            operation.getFromPan(),
            operation.getToPan(),
            operation.getStatus(),
            operation.getFailureReason(),
            operation.getCreatedAt(),
            operation.getUpdatedAt()
        );
    }
}
