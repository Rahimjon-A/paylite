package com.paylite.web.rest;

import com.paylite.domain.Agent;
import com.paylite.domain.dto.AgentBalanceResponse;
import com.paylite.domain.dto.TopUpRequest;
import com.paylite.service.AgentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @GetMapping("/me/balance")
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<AgentBalanceResponse> getCurrentAgentBalance() {
        return ResponseEntity.ok(agentService.getCurrentAgentBalance());
    }

    @PostMapping("/{id}/topup")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Agent> topUp(@PathVariable Long id, @Valid @RequestBody TopUpRequest request) {
        return ResponseEntity.ok(agentService.topUp(id, request.amount()));
    }
}
