package com.paylite.service.impl;

import com.paylite.domain.Agent;
import com.paylite.repository.AgentRepository;
import com.paylite.service.AgentService;
import java.util.List;
import java.util.Optional;
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
    public Agent save(Agent agent) {
        LOG.debug("Request to save Agent : {}", agent);
        return agentRepository.save(agent);
    }

    @Override
    public Agent update(Agent agent) {
        LOG.debug("Request to update Agent : {}", agent);
        return agentRepository.save(agent);
    }

    @Override
    public Optional<Agent> partialUpdate(Agent agent) {
        LOG.debug("Request to partially update Agent : {}", agent);

        return agentRepository
            .findById(agent.getId())
            .map(existingAgent -> {
                if (agent.getLogin() != null) {
                    existingAgent.setLogin(agent.getLogin());
                }
                if (agent.getBalance() != null) {
                    existingAgent.setBalance(agent.getBalance());
                }

                return existingAgent;
            })
            .map(agentRepository::save);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Agent> findAll() {
        LOG.debug("Request to get all Agents");
        return agentRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Agent> findOne(Long id) {
        LOG.debug("Request to get Agent : {}", id);
        return agentRepository.findById(id);
    }

    @Override
    public void delete(Long id) {
        LOG.debug("Request to delete Agent : {}", id);
        agentRepository.deleteById(id);
    }
}
