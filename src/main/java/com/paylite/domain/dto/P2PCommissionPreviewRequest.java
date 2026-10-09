package com.paylite.domain.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record P2PCommissionPreviewRequest(@NotBlank String fromPan, @NotBlank String toPan, @NotNull @Positive Long amount) {}
