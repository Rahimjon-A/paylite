package com.paylite.web.rest;

import com.paylite.domain.dto.BindCardRequest;
import com.paylite.service.AgentCardService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing {@link com.paylite.domain.AgentCard}.
 */
@RestController
@RequestMapping("/api/agent-cards")
public class AgentCardResource {

    private static final Logger LOG = LoggerFactory.getLogger(AgentCardResource.class);
    private final AgentCardService agentCardService;

    public AgentCardResource(AgentCardService agentCardService) {
        this.agentCardService = agentCardService;
    }

    @PostMapping("/bind")
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<Void> bindCard(@Valid @RequestBody BindCardRequest request) {
        LOG.info("Receive request (bind) card {} in paylite-service", request.pan());

        agentCardService.bindCard(request);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/bind/{pan}")
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<Void> unbindCard(@PathVariable("pan") String pan) {
        LOG.info("Receive request (unbind) card {} in paylite-service", pan);

        agentCardService.unbindCard(pan);
        return ResponseEntity.noContent().build();
    }
}
