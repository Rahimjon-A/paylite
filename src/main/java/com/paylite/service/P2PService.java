package com.paylite.service;

import com.paylite.domain.dto.P2PCommissionPreviewRequest;
import com.paylite.domain.dto.P2PCommissionPreviewResponse;
import com.paylite.domain.dto.P2PRequest;
import com.paylite.domain.dto.P2PResponse;

public interface P2PService {
    P2PResponse execute(P2PRequest request);
    P2PCommissionPreviewResponse previewCommission(P2PCommissionPreviewRequest request);
}
