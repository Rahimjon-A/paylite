package com.paylite.domain.dto;

import com.paylite.domain.enumeration.AgentCardStatus;
import com.paylite.domain.enumeration.AgentCardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;

public record BindCardRequest(
    @NotBlank String pan,
    @NotNull AgentCardType type,
    @NotNull LocalDate expireDate,
    @NotNull AgentCardStatus status,
    @NotNull Instant createdAt
) {}
