package com.paylite.service.impl;

import com.paylite.client.CardBankClient;
import com.paylite.domain.Agent;
import com.paylite.domain.AgentCard;
import com.paylite.domain.dto.BindCardRequest;
import com.paylite.domain.dto.CardResponse;
import com.paylite.repository.AgentCardRepository;
import com.paylite.repository.AgentRepository;
import com.paylite.security.SecurityUtils;
import com.paylite.service.AgentCardService;
import com.paylite.web.rest.errors.BadRequestAlertException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link com.paylite.domain.AgentCard}.
 */
@Service
public class AgentCardServiceImpl implements AgentCardService {

    private static final Logger LOG = LoggerFactory.getLogger(AgentCardServiceImpl.class);

    private final AgentRepository agentRepository;
    private final AgentCardRepository agentCardRepository;

    public AgentCardServiceImpl(AgentRepository agentRepository, AgentCardRepository agentCardRepository, CardBankClient cardBankClient) {
        this.agentRepository = agentRepository;
        this.agentCardRepository = agentCardRepository;
    }

    @Override
    @Transactional
    public AgentCard bindCard(BindCardRequest request) {
        String login = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new IllegalStateException("Current user is not authenticated"));

        if (!login.equals(request.createdByLogin())) {
            throw new BadRequestAlertException("Only the agent who created this card can bind it", "agentCard", "cardowner-mismatch");
        }

        Agent agent = agentRepository
            .findOneByLogin(login)
            .orElseThrow(() -> new BadRequestAlertException("Agent not found", "agentCard", "agentnotfound"));

        if (agentCardRepository.existsByPan(request.pan())) {
            throw new BadRequestAlertException("Card is already bound to an agent", "agentCard", "cardalreadybound");
        }

        AgentCard agentCard = new AgentCard()
            .pan(request.pan())
            .type(request.type())
            .expireDate(request.expireDate())
            .status(request.status())
            .createdAt(request.createdAt())
            .agent(agent);

        return agentCardRepository.save(agentCard);
    }

    @Override
    @Transactional
    public void unbindCard(String pan) {
        LOG.info("Receive request (unbind) card {} in paylite-service", pan);

        agentCardRepository.deleteAgentCardByPan(pan);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isAgentCard(String pan, String login) {
        LOG.debug("Receive request check agent's card pan: {}, agent login: {}", pan, login);

        return agentCardRepository.existsByPanAndAgent_Login(pan, login);
    }
}
