package com.paylite.domain.dto;

import com.paylite.domain.enumeration.P2POperationStatus;
import java.time.Instant;

public record P2PResponse(
    String requestId,
    Long amount,
    Long commissionAmount,
    Long totalAmount,
    String fromPan,
    String toPan,
    P2POperationStatus status,
    String failureReason,
    Instant createdAt,
    Instant updatedAt
) {}
