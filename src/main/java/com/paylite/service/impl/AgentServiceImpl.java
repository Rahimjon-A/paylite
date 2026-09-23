package com.paylite.service.impl;

import com.paylite.domain.Agent;
import com.paylite.domain.dto.AgentBalanceResponse;
import com.paylite.repository.AgentRepository;
import com.paylite.security.SecurityUtils;
import com.paylite.service.AgentService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.paylite.domain.Agent}.
 */
@Service
@Transactional
public class AgentServiceImpl implements AgentService {

    private static final Logger LOG = LoggerFactory.getLogger(AgentServiceImpl.class);

    private final AgentRepository agentRepository;

    public AgentServiceImpl(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public AgentBalanceResponse getCurrentAgentBalance() {
        String login = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("Current user is not authenticated"));

        Agent agent = agentRepository.findOneByLogin(login).orElseThrow(() -> new EntityNotFoundException("Agent not found: " + login));

        return new AgentBalanceResponse(agent.getBalance());
    }

    @Override
    public Agent topUp(Long id, Long amount) {
        LOG.debug("Request to top up Agent : {}, amount : {}", id, amount);

        Agent agent = agentRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Agent not found: " + id));

        agent.setBalance(agent.getBalance() + amount);

        return agentRepository.save(agent);
    }
}
