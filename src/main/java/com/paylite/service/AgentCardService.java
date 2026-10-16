package com.paylite.service;

import com.paylite.domain.AgentCard;
import com.paylite.domain.dto.BindCardRequest;

public interface AgentCardService {
    AgentCard bindCard(BindCardRequest request);
    void unbindCard(String pan);
    boolean isAgentCard(String pan, String login);
}
