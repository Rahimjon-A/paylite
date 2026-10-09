package com.paylite.service.card;

import com.paylite.client.HumoClient;
import com.paylite.domain.dto.BalanceOperationRequest;
import com.paylite.domain.enumeration.AgentCardType;
import org.springframework.stereotype.Component;

@Component
public class HumoNetworkClient implements CardNetworkClient {

    private final HumoClient humoClient;

    public HumoNetworkClient(HumoClient humoClient) {
        this.humoClient = humoClient;
    }

    @Override
    public AgentCardType getType() {
        return AgentCardType.HUMO;
    }

    @Override
    public Long getBalance(String pan) {
        return humoClient.getAccount(pan).balance();
    }

    @Override
    public void withdraw(String pan, Long amount) {
        humoClient.withdraw(pan, new BalanceOperationRequest(amount));
    }

    @Override
    public void deposit(String pan, Long amount) {
        humoClient.deposit(pan, new BalanceOperationRequest(amount));
    }
}
