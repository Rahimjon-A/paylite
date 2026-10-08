package com.paylite.domain.dto;

import com.paylite.domain.enumeration.AgentCardStatus;
import com.paylite.domain.enumeration.AgentCardType;
import java.time.LocalDate;

public record CardInfo(String pan, AgentCardType type, LocalDate expireDate, AgentCardStatus status) {}
