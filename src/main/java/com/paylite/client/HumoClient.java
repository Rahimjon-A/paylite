package com.paylite.client;

import com.paylite.config.FeignSecurityConfig;
import com.paylite.config.HumoFeignConfig;
import com.paylite.domain.dto.BalanceOperationRequest;
import com.paylite.domain.dto.CardAccountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "humo-service", configuration = { HumoFeignConfig.class, FeignSecurityConfig.class })
public interface HumoClient {
    @GetMapping(value = "/api/humo/card-accounts/{pan}", produces = MediaType.APPLICATION_XML_VALUE)
    CardAccountResponse getAccount(@PathVariable("pan") String pan);

    @PostMapping(
        value = "/api/humo/card-accounts/{pan}/deposit",
        consumes = MediaType.APPLICATION_XML_VALUE,
        produces = MediaType.APPLICATION_XML_VALUE
    )
    CardAccountResponse deposit(@PathVariable("pan") String pan, @RequestBody BalanceOperationRequest request);

    @PostMapping(
        value = "/api/humo/card-accounts/{pan}/withdraw",
        consumes = MediaType.APPLICATION_XML_VALUE,
        produces = MediaType.APPLICATION_XML_VALUE
    )
    CardAccountResponse withdraw(@PathVariable("pan") String pan, @RequestBody BalanceOperationRequest request);
}
