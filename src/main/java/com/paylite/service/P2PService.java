package com.paylite.service;

import com.paylite.domain.dto.P2PRequest;
import com.paylite.domain.dto.P2PResponse;

public interface P2PService {
    P2PResponse execute(P2PRequest request);
}
