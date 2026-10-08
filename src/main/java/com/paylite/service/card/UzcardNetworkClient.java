package com.paylite.service.card;

import com.paylite.client.UzcardClient;
import com.paylite.domain.dto.BalanceOperationRequest;
import com.paylite.domain.enumeration.AgentCardType;
import org.springframework.stereotype.Component;

@Component
public class UzcardNetworkClient implements CardNetworkClient {

    private final UzcardClient uzcardClient;

    public UzcardNetworkClient(UzcardClient uzcardClient) {
        this.uzcardClient = uzcardClient;
    }

    @Override
    public AgentCardType getType() {
        return AgentCardType.UZCARD;
    }

    @Override
    public Long getBalance(String pan) {
        return uzcardClient.getAccount(pan).balance();
    }

    @Override
    public void withdraw(String pan, Long amount) {
        uzcardClient.withdraw(pan, new BalanceOperationRequest(amount));
    }

    @Override
    public void deposit(String pan, Long amount) {
        uzcardClient.deposit(pan, new BalanceOperationRequest(amount));
    }
}
