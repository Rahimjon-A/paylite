package com.paylite.service;

import com.paylite.domain.Agent;
import com.paylite.domain.dto.AgentBalanceResponse;
import java.util.List;
import java.util.Optional;

/**
 * Service Interface for managing {@link com.paylite.domain.Agent}.
 */
public interface AgentService {
    Agent getCurrentAgent();
    Agent topUp(Long id, Long amount);
}
