package com.paylite.repository;

import com.paylite.domain.AgentCard;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the AgentCard entity.
 */
@Repository
public interface AgentCardRepository extends JpaRepository<AgentCard, Long> {
    Optional<AgentCard> findByPan(String pan);

    boolean existsByPan(String pan);

    List<AgentCard> findAllByAgentId(Long agentId);
}
