package com.paylite.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.paylite.config.ApplicationProperties;
import com.paylite.domain.Agent;
import com.paylite.domain.Payment;
import com.paylite.domain.enumeration.PaymentStatus;
import com.paylite.repository.AgentRepository;
import com.paylite.repository.PaymentRepository;
import com.paylite.security.SecurityUtils;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class PaymentServiceImplTest {

    private PaymentRepository paymentRepository;
    private AgentRepository agentRepository;
    private ApplicationProperties applicationProperties;

    private PaymentServiceImpl paymentService;

    @BeforeEach
    void setUp() {
        paymentRepository = mock(PaymentRepository.class);
        agentRepository = mock(AgentRepository.class);
        applicationProperties = new ApplicationProperties();

        applicationProperties.getPayment().setCommissionPercent(new BigDecimal("1.5"));

        paymentService = new PaymentServiceImpl(paymentRepository, agentRepository, applicationProperties);
    }

    @Test
    void shouldCalculateCommissionAndCreatePaidPayment() {
        Agent agent = new Agent();
        agent.setId(1L);
        agent.setLogin("agent");
        agent.setBalance(1_000_000L);

        Payment payment = new Payment().accountNumber("998901234567").amount(500_000L);

        when(agentRepository.findOneByLoginForUpdate("agent")).thenReturn(Optional.of(agent));

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment savedPayment = invocation.getArgument(0);
            savedPayment.setId(1L);
            return savedPayment;
        });

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("agent"));

            Payment result = paymentService.createPayment(payment);

            assertThat(result.getAmount()).isEqualTo(500_000L);

            assertThat(result.getCommissionAmount()).isEqualTo(7_500L);

            assertThat(result.getTotalAmount()).isEqualTo(507_500L);

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.PAID);

            assertThat(result.getAgent()).isSameAs(agent);

            assertThat(agent.getBalance()).isEqualTo(492_500L);

            verify(paymentRepository).save(payment);
            verify(agentRepository).save(agent);
        }
    }

    @Test
    void shouldCreateFailedPaymentAndKeepBalanceUnchanged() {
        Agent agent = new Agent();
        agent.setId(1L);
        agent.setLogin("agent");
        agent.setBalance(100_000L);

        Payment payment = new Payment().accountNumber("998901234567").amount(200_000L);

        when(agentRepository.findOneByLoginForUpdate("agent")).thenReturn(Optional.of(agent));

        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> {
            Payment savedPayment = invocation.getArgument(0);
            savedPayment.setId(1L);
            return savedPayment;
        });

        try (MockedStatic<SecurityUtils> securityUtils = mockStatic(SecurityUtils.class)) {
            securityUtils.when(SecurityUtils::getCurrentUserLogin).thenReturn(Optional.of("agent"));

            Payment result = paymentService.createPayment(payment);

            assertThat(result.getStatus()).isEqualTo(PaymentStatus.FAILED);

            assertThat(result.getCommissionAmount()).isEqualTo(3_000L);

            assertThat(result.getTotalAmount()).isEqualTo(203_000L);

            assertThat(result.getAgent()).isSameAs(agent);

            assertThat(agent.getBalance()).isEqualTo(100_000L);

            verify(paymentRepository).save(payment);
            verify(agentRepository, never()).save(any(Agent.class));
        }
    }
}
