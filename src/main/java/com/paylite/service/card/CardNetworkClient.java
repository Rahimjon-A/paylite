package com.paylite.service.card;

import com.paylite.domain.enumeration.AgentCardType;

public interface CardNetworkClient {
    AgentCardType getType();

    Long getBalance(String pan);

    void withdraw(String pan, Long amount);

    void deposit(String pan, Long amount);
}
