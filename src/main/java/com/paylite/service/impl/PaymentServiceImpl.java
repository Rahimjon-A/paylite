package com.paylite.service.impl;

import com.paylite.config.ApplicationProperties;
import com.paylite.domain.Agent;
import com.paylite.domain.Payment;
import com.paylite.domain.enumeration.PaymentStatus;
import com.paylite.repository.AgentRepository;
import com.paylite.repository.PaymentRepository;
import com.paylite.security.SecurityUtils;
import com.paylite.service.PaymentService;
import com.paylite.util.CommissionCalculator;
import jakarta.persistence.EntityNotFoundException;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Payment createPayment(Payment payment) {
        String login = getCurrentAgentLogin();

        LOG.debug(
            "Request to create Payment for Agent : {}, account : {}, amount : {}",
            login,
            payment.getAccountNumber(),
            payment.getAmount()
        );

        Agent agent = findAgentForUpdate(login);

        payment.setAgent(agent);
        payment.setCommissionAmount(CommissionCalculator.getCommissionAmount(payment.getAmount(), applicationProperties));

        long totalAmount = payment.getAmount() + payment.getCommissionAmount();
        payment.setTotalAmount(totalAmount);
        payment.setCreatedDate(Instant.now());

        if (agent.getBalance() < totalAmount) {
            return createFailedPayment(payment);
        }

        return createPaidPayment(payment, agent);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Payment> getCurrentAgentPayments(Pageable pageable) {
        String login = getCurrentAgentLogin();

        LOG.debug("Request to get payments for Agent : {}", login);

        return paymentRepository.findAllByAgentLogin(login, pageable);
    }

    private String getCurrentAgentLogin() {
        return SecurityUtils.getCurrentUserLogin().orElseThrow(() -> new IllegalStateException("Current user is not authenticated"));
    }

    private Agent findAgentForUpdate(String login) {
        return agentRepository.findOneByLoginForUpdate(login).orElseThrow(() -> new EntityNotFoundException("Agent not found: " + login));
    }

    private Payment createFailedPayment(Payment payment) {
        LOG.debug(
            "Insufficient balance for Agent : {}, required : {}, available : {}",
            payment.getAgent().getLogin(),
            payment.getTotalAmount(),
            payment.getAgent().getBalance()
        );

        payment.setStatus(PaymentStatus.FAILED);

        return paymentRepository.save(payment);
    }

    private Payment createPaidPayment(Payment payment, Agent agent) {
        agent.setBalance(agent.getBalance() - payment.getTotalAmount());

        payment.setStatus(PaymentStatus.PAID);

        agentRepository.save(agent);

        LOG.debug(
            "Payment successfully created for Agent : {}, total amount : {}, remaining balance : {}",
            agent.getLogin(),
            payment.getTotalAmount(),
            agent.getBalance()
        );

        return paymentRepository.save(payment);
    }
}
