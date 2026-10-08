package com.paylite.domain.dto;

import java.time.Instant;
import java.time.LocalDate;

public record CardResponse(
    Long id,
    String pan,
    String type,
    LocalDate expireDate,
    String fullName,
    String phoneNumber,
    String status,
    Instant createdAt
) {}
