package com.paylite.domain.dto;

public record P2PCommissionPreviewResponse(
    String fromType,
    String toType,
    Long amount,
    java.math.BigDecimal commissionPercent,
    Long commissionAmount,
    Long totalAmount
) {}
