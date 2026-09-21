package com.paylite.web.rest;

import com.paylite.domain.Agent;
import com.paylite.service.AgentService;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.paylite.domain.Agent}.
 */
@RestController
@RequestMapping("/api/agents")
public class AgentResource {

    private static final Logger LOG = LoggerFactory.getLogger(AgentResource.class);

    private final AgentService agentService;

    public AgentResource(AgentService agentService) {
        this.agentService = agentService;
    }

    /**
     * {@code GET  /agents} : get all the agents.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of agents in body.
     */
    @GetMapping("")
    public List<Agent> getAllAgents() {
        LOG.debug("REST request to get all Agents");
        return agentService.findAll();
    }

    /**
     * {@code GET  /agents/:id} : get the "id" agent.
     *
     * @param id the id of the agent to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the agent, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Agent> getAgent(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Agent : {}", id);
        Optional<Agent> agent = agentService.findOne(id);
        return ResponseUtil.wrapOrNotFound(agent);
    }
}
