package com.paylite.service.impl;

import com.paylite.config.ApplicationProperties;
import com.paylite.domain.Agent;
import com.paylite.domain.Payment;
import com.paylite.domain.dto.CreatePaymentRequest;
import com.paylite.domain.dto.PaymentResponse;
import com.paylite.domain.dto.PaymentResult;
import com.paylite.domain.enumeration.PaymentStatus;
import com.paylite.repository.AgentRepository;
import com.paylite.repository.PaymentRepository;
import com.paylite.security.SecurityUtils;
import com.paylite.service.PaymentService;
import jakarta.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.paylite.domain.Payment}.
 */
@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final AgentRepository agentRepository;
    private final ApplicationProperties applicationProperties;

    public PaymentServiceImpl(
        PaymentRepository paymentRepository,
        AgentRepository agentRepository,
        ApplicationProperties applicationProperties
    ) {
        this.paymentRepository = paymentRepository;
        this.agentRepository = agentRepository;
        this.applicationProperties = applicationProperties;
    }

    @Override
    public PaymentResult createPayment(CreatePaymentRequest request) {
        String login = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("Current user is not authenticated"));

        Agent agent = agentRepository
            .findOneByLoginForUpdate(login)
            .orElseThrow(() -> new EntityNotFoundException("Agent not found: " + login));

        Long amount = request.amount();

        long commissionAmount = getCommissionAmount(amount);
        long totalAmount = amount + commissionAmount;

        Payment payment = new Payment()
            .accountNumber(request.account())
            .amount(amount)
            .commissionAmount(commissionAmount)
            .totalAmount(totalAmount)
            .createdDate(Instant.now())
            .agent(agent);

        if (agent.getBalance() < totalAmount) {
            payment.setStatus(PaymentStatus.FAILED);

            paymentRepository.save(payment);

            return new PaymentResult(toResponse(payment), true);
        }

        agent.setBalance(agent.getBalance() - totalAmount);
        payment.setStatus(PaymentStatus.PAID);

        paymentRepository.save(payment);
        agentRepository.save(agent);

        return new PaymentResult(toResponse(payment), false);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PaymentResponse> getCurrentAgentPayments(Pageable pageable) {
        String login = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("Current user is not authenticated"));

        return paymentRepository.findAllByAgentLogin(login, pageable).map(this::toResponse);
    }

    private long getCommissionAmount(Long amount) {
        BigDecimal commissionPercent = applicationProperties.getPayment().getCommissionPercent();

        if (commissionPercent == null) {
            throw new IllegalStateException(
                "Commission percentage is not configured. " + "Expected property: application.payment.commission-percent"
            );
        }

        BigDecimal commission = BigDecimal.valueOf(amount)
            .multiply(commissionPercent)
            .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        return commission.longValueExact();
    }

    private PaymentResponse toResponse(Payment payment) {
        return new PaymentResponse(
            payment.getId(),
            payment.getAccountNumber(),
            payment.getAmount(),
            payment.getCommissionAmount(),
            payment.getTotalAmount(),
            payment.getStatus(),
            payment.getCreatedDate()
        );
    }
}
