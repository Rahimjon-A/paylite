package com.paylite.client;

import com.paylite.config.FeignSecurityConfig;
import com.paylite.domain.dto.BalanceOperationRequest;
import com.paylite.domain.dto.CardAccountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "uzcard-service", configuration = FeignSecurityConfig.class)
public interface UzcardClient {
    @GetMapping("/api/uzcard/card-accounts/{pan}")
    CardAccountResponse getAccount(@PathVariable("pan") String pan);

    @PostMapping("/api/uzcard/card-accounts/{pan}/withdraw")
    CardAccountResponse withdraw(@PathVariable("pan") String pan, @RequestBody BalanceOperationRequest request);

    @PostMapping("/api/uzcard/card-accounts/{pan}/deposit")
    CardAccountResponse deposit(@PathVariable("pan") String pan, @RequestBody BalanceOperationRequest request);
}
