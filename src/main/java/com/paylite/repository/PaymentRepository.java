package com.paylite.repository;

import com.paylite.domain.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Payment entity.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    @Query(
        """
        select payment
        from Payment payment
        where payment.agent.login = :login
        order by payment.createdDate desc
        """
    )
    Page<Payment> findAllByAgentLogin(@Param("login") String login, Pageable pageable);
}
