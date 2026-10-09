package com.paylite.service.impl;

import com.paylite.config.ApplicationProperties;
import com.paylite.config.CommissionProperties;
import com.paylite.domain.enumeration.AgentCardType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import org.springframework.stereotype.Service;

@Service
public class CommissionService {

    private final CommissionProperties properties;
    private final ApplicationProperties applicationProperties;

    public CommissionService(CommissionProperties properties, ApplicationProperties applicationProperties) {
        this.properties = properties;
        this.applicationProperties = applicationProperties;
    }

    public long calculate(AgentCardType fromType, AgentCardType toType, long amount) {
        BigDecimal percentage = getPercentage(fromType, toType);

        if (percentage == null) {
            throw new IllegalStateException("Commission percentage is not configured. " + "Expected property: paylite.commission...");
        }

        return BigDecimal.valueOf(amount).multiply(percentage).divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP).longValueExact();
    }

    public long getCommissionAmount(Long amount) {
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

    public BigDecimal getCommissionPercent(AgentCardType fromType, AgentCardType toType) {
        return getPercentage(fromType, toType);
    }

    private BigDecimal getPercentage(AgentCardType fromType, AgentCardType toType) {
        return switch (fromType) {
            case UZCARD -> switch (toType) {
                case UZCARD -> properties.getUzcardToUzcard();
                case HUMO -> properties.getUzcardToHumo();
            };
            case HUMO -> switch (toType) {
                case UZCARD -> properties.getHumoToUzcard();
                case HUMO -> properties.getHumoToHumo();
            };
        };
    }
}
