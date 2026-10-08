package com.paylite.repository;

import com.paylite.domain.P2POperation;
import java.util.Optional;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the P2POperation entity.
 */
@SuppressWarnings("unused")
@Repository
public interface P2PRepository extends JpaRepository<P2POperation, Long> {
    Optional<P2POperation> findByRequestId(String requestId);
}
