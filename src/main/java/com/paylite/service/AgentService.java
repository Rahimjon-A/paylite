package com.paylite.service;

import com.paylite.domain.Agent;

/**
 * Service Interface for managing {@link com.paylite.domain.Agent}.
 */
public interface AgentService {
    Agent getCurrentAgent();
    Agent topUp(Long id, Long amount);
}
