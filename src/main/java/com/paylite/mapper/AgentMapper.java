package com.paylite.mapper;

import com.paylite.domain.Agent;
import com.paylite.domain.dto.AgentBalanceResponse;
import com.paylite.domain.dto.AgentResponse;
import com.paylite.domain.dto.TopUpRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AgentMapper {
    AgentResponse toResponse(Agent agent);

    AgentBalanceResponse toBalanceResponse(Agent agent);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "login", ignore = true)
    Agent toEntity(TopUpRequest request);
}
