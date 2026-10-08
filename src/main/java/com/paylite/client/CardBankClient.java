package com.paylite.client;

import com.paylite.config.FeignSecurityConfig;
import com.paylite.domain.dto.CardResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "card-bank-service", configuration = FeignSecurityConfig.class)
public interface CardBankClient {
    @GetMapping("/api/cards/{pan}")
    CardResponse getCardByPan(@PathVariable String pan);
}
