package com.paylite.web.rest;

import com.paylite.domain.dto.P2PRequest;
import com.paylite.domain.dto.P2PResponse;
import com.paylite.service.impl.P2PServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing {@link com.paylite.domain.P2POperation}.
 */
@RestController
@RequestMapping("/api/p2p")
public class P2PResource {

    private final P2PServiceImpl p2PServiceImpl;

    public P2PResource(P2PServiceImpl p2PServiceImpl) {
        this.p2PServiceImpl = p2PServiceImpl;
    }

    @PostMapping
    public ResponseEntity<P2PResponse> execute(@Valid @RequestBody P2PRequest request) {
        P2PResponse response = p2PServiceImpl.execute(request);

        return ResponseEntity.ok(response);
    }
}
