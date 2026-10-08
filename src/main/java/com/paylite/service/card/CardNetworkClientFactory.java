package com.paylite.service.card;

import com.paylite.domain.enumeration.AgentCardType;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class CardNetworkClientFactory {

    private final Map<AgentCardType, CardNetworkClient> clients;

    public CardNetworkClientFactory(List<CardNetworkClient> clients) {
        this.clients = clients.stream().collect(Collectors.toMap(CardNetworkClient::getType, Function.identity()));
    }

    public CardNetworkClient get(AgentCardType type) {
        CardNetworkClient client = clients.get(type);

        if (client == null) {
            throw new IllegalArgumentException("Unsupported card type: " + type);
        }

        return client;
    }
}
