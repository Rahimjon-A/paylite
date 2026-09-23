package com.paylite.repository;

import com.paylite.domain.Agent;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Agent entity.
 */
@SuppressWarnings("unused")
@Repository
public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findOneByLogin(String login);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select agent from Agent agent where agent.login = :login")
    Optional<Agent> findOneByLoginForUpdate(@Param("login") String login);
}
