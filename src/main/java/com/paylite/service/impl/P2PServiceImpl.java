package com.paylite.service.impl;

import com.paylite.client.CardBankClient;
import com.paylite.domain.CommissionLedger;
import com.paylite.domain.P2POperation;
import com.paylite.domain.dto.*;
import com.paylite.domain.enumeration.AgentCardStatus;
import com.paylite.domain.enumeration.AgentCardType;
import com.paylite.domain.enumeration.P2POperationStatus;
import com.paylite.repository.P2PRepository;
import com.paylite.security.SecurityUtils;
import com.paylite.service.AgentCardService;
import com.paylite.service.CommissionLedgerService;
import com.paylite.service.P2PService;
import com.paylite.service.card.CardNetworkClient;
import com.paylite.service.card.CardNetworkClientFactory;
import com.paylite.web.rest.errors.P2PCardException;
import com.paylite.web.rest.errors.P2PInsufficientBalanceException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
public class P2PServiceImpl implements P2PService {

    private static final Logger LOG = LoggerFactory.getLogger(P2PServiceImpl.class);
    private final P2PRepository operationRepository;
    private final CardBankClient cardBankClient;
    private final CardValidationService cardValidationService;
    private final CommissionService commissionService;
    private final CardNetworkClientFactory networkClientFactory;
    private final AgentCardService agentCardService;
    private final CommissionLedgerService commissionLedgerService;

    public P2PServiceImpl(
        P2PRepository operationRepository,
        CardBankClient cardBankClient,
        CardValidationService cardValidationService,
        CommissionService commissionService,
        CardNetworkClientFactory networkClientFactory,
        AgentCardService agentCardService,
        CommissionLedgerService commissionLedgerService
    ) {
        this.operationRepository = operationRepository;
        this.cardBankClient = cardBankClient;
        this.cardValidationService = cardValidationService;
        this.commissionService = commissionService;
        this.networkClientFactory = networkClientFactory;
        this.agentCardService = agentCardService;
        this.commissionLedgerService = commissionLedgerService;
    }

    @Override
    public P2PResponse execute(P2PRequest request) {
        LOG.info("Starting P2P operation: requestId={}, amount={}", request.requestId(), request.amount());

        // 0. Verify sender card ownership BEFORE processing
        validateSenderCardOwnership(request.fromPan());

        // 1. Idempotency check
        Optional<P2POperation> existingOperation = findExistingOperation(request.requestId());

        if (existingOperation.isPresent()) {
            LOG.info("P2P operation already exists: requestId={}, status={}", request.requestId(), existingOperation.get().getStatus());

            return toResponse(existingOperation.get());
        }

        // 2. Get and validate cards
        CardInfo fromCard = getAndValidateCard(request.fromPan());
        CardInfo toCard = getAndValidateCard(request.toPan());

        validateDifferentCards(fromCard, toCard);

        // 3. Calculate commission
        long commission = commissionService.calculate(fromCard.type(), toCard.type(), request.amount());
        long totalAmount = calculateTotalAmount(request.amount(), commission);

        LOG.debug("Calculated P2P amounts: requestId={}, commission={}, totalAmount={}", request.requestId(), commission, totalAmount);

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
        LOG.info("P2P operation finished: requestId={}, status={}", operation.getRequestId(), operation.getStatus());
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
            LOG.warn("P2P operation claim conflict: requestId={}", operation.getRequestId());

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
            LOG.error("Unable to retrieve card information: PAN={}", maskPan(pan), exception);

            throw new P2PCardException("Unable to get card information for PAN: " + pan, exception);
        }
    }

    private void validateSenderCardOwnership(String pan) {
        String login = SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new AccessDeniedException("User is not authenticated"));

        if (!agentCardService.isAgentCard(pan, login)) {
            throw new AccessDeniedException("You are not authorized to use this sender card");
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
            LOG.warn("Insufficient sender balance: PAN={}, requiredAmount={}", maskPan(senderPan), totalAmount);

            throw exception;
        } catch (Exception exception) {
            LOG.error("Unable to check sender balance: PAN={}", maskPan(senderPan), exception);

            throw new P2PCardException("Unable to check sender balance", exception);
        }
    }

    private void withdraw(P2POperation operation, CardNetworkClient senderClient, String senderPan, long totalAmount) {
        try {
            senderClient.withdraw(senderPan, totalAmount);
            saveOperation(operation, P2POperationStatus.WITHDRAWN);
        } catch (Exception exception) {
            LOG.error(
                "Sender withdrawal failed: requestId={}, PAN={}, amount={}",
                operation.getRequestId(),
                maskPan(senderPan),
                totalAmount,
                exception
            );

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
            recordCommission(operation);

            LOG.info("Recipient payment completed: requestId={}, amount={}", operation.getRequestId(), amount);

            return toResponse(operation);
        } catch (Exception paymentException) {
            LOG.error("Recipient deposit failed: requestId={}. Starting compensation.", operation.getRequestId(), paymentException);

            return compensate(operation, senderClient, senderPan, totalAmount, paymentException);
        }
    }

    private void recordCommission(P2POperation operation) {
        CommissionLedger entry = new CommissionLedger()
            .requestId(operation.getRequestId())
            .fromPan(operation.getFromPan())
            .cardType(operation.getFromType())
            .destinationAccount("SYSTEM_COMMISSION_ACCOUNT")
            .commissionAmount(operation.getCommissionAmount())
            .totalAmount(operation.getTotalAmount())
            .transferredAmount(operation.getAmount())
            .createdAt(Instant.now());

        commissionLedgerService.record(entry);
    }

    private P2PResponse compensate(
        P2POperation operation,
        CardNetworkClient senderClient,
        String senderPan,
        long amount,
        Exception paymentException
    ) {
        saveOperation(operation, P2POperationStatus.COMPENSATING);

        try {
            senderClient.deposit(senderPan, amount);
            operation.setFailureReason("Recipient payment failed; funds returned");
            saveOperation(operation, P2POperationStatus.COMPENSATED);

            LOG.warn(
                "P2P operation compensated successfully: requestId={}. " + "The recipient deposit failed, but sender funds were returned.",
                operation.getRequestId()
            );

            return toResponse(operation);
        } catch (Exception compensationException) {
            operation.setFailureReason("Payment failed and compensation failed");
            saveOperation(operation, P2POperationStatus.COMPENSATION_FAILED);

            compensationException.addSuppressed(paymentException);

            LOG.error(
                "P2P compensation failed: requestId={}. " + "Both recipient deposit and sender refund failed.",
                operation.getRequestId(),
                compensationException
            );

            throw new P2PCardException("Payment failed and compensation failed", compensationException);
        }
    }

    private void failOperation(P2POperation operation, String reason) {
        operation.setFailureReason(reason);
        saveOperation(operation, P2POperationStatus.FAILED);
    }

    private String maskPan(String pan) {
        if (pan == null || pan.length() < 4) {
            return "****";
        }
        return "**** **** **** " + pan.substring(pan.length() - 4);
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
