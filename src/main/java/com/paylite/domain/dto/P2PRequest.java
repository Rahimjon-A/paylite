package com.paylite.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record P2PRequest(
    @NotBlank String requestId,

    @NotNull @Positive Long amount,

    @NotBlank String fromPan,

    @NotBlank String toPan
) {}
