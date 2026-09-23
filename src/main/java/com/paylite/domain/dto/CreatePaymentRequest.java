package com.paylite.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreatePaymentRequest(
    @NotBlank(message = "Account is required") String account,

    @NotNull(message = "Amount is required") @Positive(message = "Amount must be greater than zero") Long amount
) {}
