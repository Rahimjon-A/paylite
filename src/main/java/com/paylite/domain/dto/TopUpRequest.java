package com.paylite.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TopUpRequest(@NotNull(message = "Amount is required") @Positive(message = "Amount must be greater than zero") Long amount) {}
