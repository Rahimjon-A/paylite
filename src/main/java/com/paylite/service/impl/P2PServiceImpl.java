package com.paylite.service.impl;

import com.paylite.client.CardBankClient;
import com.paylite.domain.P2POperation;
import com.paylite.domain.dto.*;
import com.paylite.domain.enumeration.AgentCardStatus;
import com.paylite.domain.enumeration.AgentCardType;
import com.paylite.domain.enumeration.P2POperationStatus;
import com.paylite.repository.P2PRepository;
import com.paylite.service.P2PService;
import com.paylite.service.card.CardNetworkClient;
import com.paylite.service.card.CardNetworkClientFactory;
import com.paylite.web.rest.errors.P2PCardException;
import com.paylite.web.rest.errors.P2PInsufficientBalanceException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.springframework.dao.DataIntegrityViolationException;
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

    @Override
    public P2PResponse execute(P2PRequest request) {
        // 1. Idempotency check
        Optional<P2POperation> existingOperation = findExistingOperation(request.requestId());

        if (existingOperation.isPresent()) {
            return toResponse(existingOperation.get());
        }

        // 2. Get and validate cards
        CardInfo fromCard = getAndValidateCard(request.fromPan());
        CardInfo toCard = getAndValidateCard(request.toPan());

        validateDifferentCards(fromCard, toCard);

        // 3. Calculate commission
        long commission = commissionService.calculate(fromCard.type(), toCard.type(), request.amount());
        long totalAmount = calculateTotalAmount(request.amount(), commission);

        // 4. Create and claim P2P operation
        P2POperation operation = createOperation(request, fromCard, toCard, commission, totalAmount);
        operation = claimOperation(operation);

        // 5. Select card-network clients
        CardNetworkClient senderClient = networkClientFactory.get(fromCard.type());
        CardNetworkClient receiverClient = networkClientFactory.get(toCard.type());

        // 6. Check sender balance
        checkBalance(senderClient, fromCard.pan(), totalAmount);
        saveOperation(operation, P2POperationStatus.BALANCE_CHECKED);

        // 7. Withdraw sender funds
        withdraw(operation, senderClient, fromCard.pan(), totalAmount);

        // 8. Pay recipient or compensate if payment fails
        return payRecipient(operation, senderClient, receiverClient, fromCard.pan(), toCard.pan(), request.amount(), totalAmount);
    }

    @Override
    public P2PCommissionPreviewResponse previewCommission(P2PCommissionPreviewRequest request) {
        CardInfo fromCard = getAndValidateCard(request.fromPan());
        CardInfo toCard = getAndValidateCard(request.toPan());

        validateDifferentCards(fromCard, toCard);

        BigDecimal commissionPercent = commissionService.getCommissionPercent(fromCard.type(), toCard.type());

        long commissionAmount = commissionService.calculate(fromCard.type(), toCard.type(), request.amount());

        long totalAmount = calculateTotalAmount(request.amount(), commissionAmount);

        return new P2PCommissionPreviewResponse(
            fromCard.type().name(),
            toCard.type().name(),
            request.amount(),
            commissionPercent,
            commissionAmount,
            totalAmount
        );
    }

    private Optional<P2POperation> findExistingOperation(String requestId) {
        return operationRepository.findByRequestId(requestId);
    }

    private P2POperation claimOperation(P2POperation operation) {
        try {
            return operationRepository.saveAndFlush(operation);
        } catch (DataIntegrityViolationException exception) {
            return operationRepository
                .findByRequestId(operation.getRequestId())
                .orElseThrow(() -> new P2PCardException("P2P operation already exists but could not be loaded", exception));
        }
    }

    private CardInfo getAndValidateCard(String pan) {
        CardInfo card = getCard(pan);
        cardValidationService.validate(card);
        return card;
    }

    private CardInfo getCard(String pan) {
        try {
            CardResponse response = cardBankClient.getCardByPan(pan);
            return toCardInfo(response);
        } catch (Exception exception) {
            throw new P2PCardException("Unable to get card information for PAN: " + pan, exception);
        }
    }

    private void validateDifferentCards(CardInfo fromCard, CardInfo toCard) {
        if (fromCard.pan().equals(toCard.pan())) {
            throw new P2PCardException("Sender and recipient cards must be different");
        }
    }

    private long calculateTotalAmount(long amount, long commission) {
        return Math.addExact(amount, commission);
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

    private P2POperation saveOperation(P2POperation operation, P2POperationStatus status) {
        operation.setStatus(status);
        operation.setUpdatedAt(Instant.now());

        return operationRepository.save(operation);
    }

    private void checkBalance(CardNetworkClient senderClient, String senderPan, long totalAmount) {
        try {
            Long balance = senderClient.getBalance(senderPan);

            if (balance < totalAmount) {
                throw new P2PInsufficientBalanceException("Insufficient balance");
            }
        } catch (P2PInsufficientBalanceException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new P2PCardException("Unable to check sender balance", exception);
        }
    }

    private void withdraw(P2POperation operation, CardNetworkClient senderClient, String senderPan, long totalAmount) {
        try {
            senderClient.withdraw(senderPan, totalAmount);
            saveOperation(operation, P2POperationStatus.WITHDRAWN);
        } catch (Exception exception) {
            failOperation(operation, "Unable to withdraw sender funds");
            throw new P2PCardException("Unable to withdraw sender funds", exception);
        }
    }

    private P2PResponse payRecipient(
        P2POperation operation,
        CardNetworkClient senderClient,
        CardNetworkClient receiverClient,
        String senderPan,
        String receiverPan,
        long amount,
        long totalAmount
    ) {
        saveOperation(operation, P2POperationStatus.PAYING);

        try {
            receiverClient.deposit(receiverPan, amount);
            saveOperation(operation, P2POperationStatus.COMPLETED);
            return toResponse(operation);
        } catch (Exception paymentException) {
            return compensate(operation, senderClient, senderPan, totalAmount);
        }
    }

    private P2PResponse compensate(P2POperation operation, CardNetworkClient senderClient, String senderPan, long amount) {
        saveOperation(operation, P2POperationStatus.COMPENSATING);

        try {
            senderClient.deposit(senderPan, amount);
            operation.setFailureReason("Recipient payment failed; funds returned");
            saveOperation(operation, P2POperationStatus.COMPENSATED);

            return toResponse(operation);
        } catch (Exception compensationException) {
            operation.setFailureReason("Payment failed and compensation failed");
            saveOperation(operation, P2POperationStatus.COMPENSATION_FAILED);

            throw new P2PCardException("Payment failed and compensation failed", compensationException);
        }
    }

    private void failOperation(P2POperation operation, String reason) {
        operation.setFailureReason(reason);
        saveOperation(operation, P2POperationStatus.FAILED);
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
