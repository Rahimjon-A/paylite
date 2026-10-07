package com.paylite.domain;

import static com.paylite.domain.AgentCardTestSamples.*;
import static com.paylite.domain.AgentTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.paylite.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class AgentCardTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(AgentCard.class);
        AgentCard agentCard1 = getAgentCardSample1();
        AgentCard agentCard2 = new AgentCard();
        assertThat(agentCard1).isNotEqualTo(agentCard2);

        agentCard2.setId(agentCard1.getId());
        assertThat(agentCard1).isEqualTo(agentCard2);

        agentCard2 = getAgentCardSample2();
        assertThat(agentCard1).isNotEqualTo(agentCard2);
    }

    @Test
    void agentTest() {
        AgentCard agentCard = getAgentCardRandomSampleGenerator();
        Agent agentBack = getAgentRandomSampleGenerator();

        agentCard.setAgent(agentBack);
        assertThat(agentCard.getAgent()).isEqualTo(agentBack);

        agentCard.agent(null);
        assertThat(agentCard.getAgent()).isNull();
    }
}
