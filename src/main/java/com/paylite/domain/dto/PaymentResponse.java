package com.paylite.domain.dto;

import com.paylite.domain.enumeration.PaymentStatus;
import java.time.Instant;

public record PaymentResponse(
    Long id,
    String account,
    Long amount,
    Long commissionAmount,
    Long totalAmount,
    PaymentStatus status,
    Instant createdDate
) {}
