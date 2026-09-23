package com.paylite.util;

import com.paylite.config.ApplicationProperties;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class CommissionCalculator {

    private CommissionCalculator() {}

    public static long getCommissionAmount(Long amount, ApplicationProperties applicationProperties) {
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
}
