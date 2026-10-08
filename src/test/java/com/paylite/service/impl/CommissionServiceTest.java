package com.paylite.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.paylite.config.ApplicationProperties;
import com.paylite.config.CommissionProperties;
import com.paylite.domain.enumeration.AgentCardType;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommissionServiceTest {

    private ApplicationProperties applicationProperties;
    private CommissionProperties commissionProperties;
    private CommissionService commissionService;

    @BeforeEach
    void setUp() {
        applicationProperties = new ApplicationProperties();
        commissionProperties = new CommissionProperties();

        commissionService = new CommissionService(commissionProperties, applicationProperties);
    }

    @Test
    void shouldCalculateCommissionAmount() {
        applicationProperties.getPayment().setCommissionPercent(new BigDecimal("1.5"));

        long commission = commissionService.getCommissionAmount(500_000L);

        assertThat(commission).isEqualTo(7_500L);
    }

    @Test
    void shouldRoundCommissionAmountHalfUp() {
        applicationProperties.getPayment().setCommissionPercent(new BigDecimal("50"));

        long commission = commissionService.getCommissionAmount(1L);

        // 1 * 50 / 100 = 0.5 → 1 tiyin with HALF_UP
        assertThat(commission).isEqualTo(1L);
    }

    @Test
    void shouldThrowExceptionWhenCommissionPercentIsNull() {
        applicationProperties.getPayment().setCommissionPercent(null);

        assertThatThrownBy(() -> commissionService.getCommissionAmount(100_000L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Expected property: application.payment.commission-percent");
    }

    @Test
    void shouldCalculateCardToCardCommission() {
        commissionProperties.setUzcardToHumo(new BigDecimal("1.0")); // 1% commission

        long commission = commissionService.calculate(AgentCardType.UZCARD, AgentCardType.HUMO, 100_000L);

        assertThat(commission).isEqualTo(1_000L);
    }

    @Test
    void shouldRoundCardToCardCommissionHalfUp() {
        commissionProperties.setHumoToHumo(new BigDecimal("50"));

        long commission = commissionService.calculate(AgentCardType.HUMO, AgentCardType.HUMO, 1L);

        // 1 * 50 / 100 = 0.5 → 1 tiyin with HALF_UP
        assertThat(commission).isEqualTo(1L);
    }

    @Test
    void shouldThrowExceptionWhenCardToCardPercentageIsNull() {
        assertThatThrownBy(() -> commissionService.calculate(AgentCardType.UZCARD, AgentCardType.UZCARD, 100_000L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Expected property: paylite.commission");
    }
}
