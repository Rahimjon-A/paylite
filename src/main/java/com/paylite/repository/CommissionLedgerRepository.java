package com.paylite.repository;

import com.paylite.domain.CommissionLedger;
import org.springframework.data.jpa.repository.*;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the CommissionLedger entity.
 */
@SuppressWarnings("unused")
@Repository
public interface CommissionLedgerRepository extends JpaRepository<CommissionLedger, Long> {}
