package com.paylite.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class AgentCardTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static AgentCard getAgentCardSample1() {
        return new AgentCard().id(1L).pan("pan1");
    }

    public static AgentCard getAgentCardSample2() {
        return new AgentCard().id(2L).pan("pan2");
    }

    public static AgentCard getAgentCardRandomSampleGenerator() {
        return new AgentCard().id(longCount.incrementAndGet()).pan(UUID.randomUUID().toString());
    }
}
