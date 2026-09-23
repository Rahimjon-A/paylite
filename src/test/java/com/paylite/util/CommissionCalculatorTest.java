package com.paylite.util;

import static org.assertj.core.api.Assertions.assertThat;

import com.paylite.config.ApplicationProperties;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CommissionCalculatorTest {

    private ApplicationProperties applicationProperties;

    @BeforeEach
    void setUp() {
        applicationProperties = new ApplicationProperties();

        applicationProperties.getPayment().setCommissionPercent(new BigDecimal("1.5"));
    }

    @Test
    void shouldCalculateCommission() {
        long commission = CommissionCalculator.getCommissionAmount(500_000L, applicationProperties);

        assertThat(commission).isEqualTo(7_500L);
    }

    @Test
    void shouldRoundCommissionHalfUp() {
        applicationProperties.getPayment().setCommissionPercent(new BigDecimal("50"));

        long commission = CommissionCalculator.getCommissionAmount(1L, applicationProperties);

        // 1 * 50 / 100 = 0.5 → 1 tiyin with HALF_UP
        assertThat(commission).isEqualTo(1L);
    }
}
