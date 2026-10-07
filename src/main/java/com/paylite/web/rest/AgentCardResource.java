package com.paylite.web.rest;

import com.paylite.domain.dto.BindCardRequest;
import com.paylite.service.AgentCardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing {@link com.paylite.domain.AgentCard}.
 */
@RestController
@RequestMapping("/api/agent-cards")
public class AgentCardResource {

    private final AgentCardService agentCardService;

    public AgentCardResource(AgentCardService agentCardService) {
        this.agentCardService = agentCardService;
    }

    @PostMapping("/bind")
    public ResponseEntity<Void> bindCard(@Valid @RequestBody BindCardRequest request) {
        agentCardService.bindCard(request);

        return ResponseEntity.ok().build();
    }
}
