//Error with Docker Deamon so postpone

//package com.paylite;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import com.paylite.domain.Agent;
//import com.paylite.repository.AgentRepository;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
//import org.testcontainers.containers.PostgreSQLContainer;
//import org.testcontainers.junit.jupiter.Container;
//import org.testcontainers.junit.jupiter.Testcontainers;
//
//@SpringBootTest(
//    properties = {
//        "spring.config.import=optional:consul:"
//    }
//)
//@Testcontainers
//class PaylitePostgresIntegrationTest {
//
//    @Container
//    @ServiceConnection
//    static PostgreSQLContainer<?> postgres =
//        new PostgreSQLContainer<>("postgres:17.4")
//            .withDatabaseName("paylite")
//            .withUsername("paylite")
//            .withPassword("paylite");
//
//    @Autowired
//    private AgentRepository agentRepository;
//
//    @Test
//    void shouldConnectToPostgresContainer() {
//        Agent agent = new Agent()
//            .login("test-agent")
//            .balance(1_000_000L);
//
//        Agent saved = agentRepository.saveAndFlush(agent);
//
//        assertThat(saved.getId()).isNotNull();
//
//        assertThat(
//            agentRepository.findOneByLogin("test-agent")
//        ).isPresent();
//    }
//}
