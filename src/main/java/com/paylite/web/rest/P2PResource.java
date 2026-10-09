package com.paylite.web.rest;

import com.paylite.domain.dto.P2PCommissionPreviewRequest;
import com.paylite.domain.dto.P2PCommissionPreviewResponse;
import com.paylite.domain.dto.P2PRequest;
import com.paylite.domain.dto.P2PResponse;
import com.paylite.service.P2PService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for managing {@link com.paylite.domain.P2POperation}.
 */
@RestController
@RequestMapping("/api/p2p")
public class P2PResource {

    private final P2PService p2PService;

    public P2PResource(P2PService p2PService) {
        this.p2PService = p2PService;
    }

    @PostMapping
    public ResponseEntity<P2PResponse> execute(@Valid @RequestBody P2PRequest request) {
        P2PResponse response = p2PService.execute(request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/commission-preview")
    public ResponseEntity<P2PCommissionPreviewResponse> previewCommission(@Valid @RequestBody P2PCommissionPreviewRequest request) {
        return ResponseEntity.ok(p2PService.previewCommission(request));
    }
}
