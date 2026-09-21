package com.paylite.service;

import com.paylite.domain.Payment;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.paylite.domain.Payment}.
 */
public interface PaymentService {
    /**
     * Save a payment.
     *
     * @param payment the entity to save.
     * @return the persisted entity.
     */
    Payment save(Payment payment);

    /**
     * Updates a payment.
     *
     * @param payment the entity to update.
     * @return the persisted entity.
     */
    Payment update(Payment payment);

    /**
     * Partially updates a payment.
     *
     * @param payment the entity to update partially.
     * @return the persisted entity.
     */
    Optional<Payment> partialUpdate(Payment payment);

    /**
     * Get all the payments.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Payment> findAll(Pageable pageable);

    /**
     * Get all the payments with eager load of many-to-many relationships.
     *
     * @param pageable the pagination information.
     * @return the list of entities.
     */
    Page<Payment> findAllWithEagerRelationships(Pageable pageable);

    /**
     * Get the "id" payment.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    Optional<Payment> findOne(Long id);

    /**
     * Delete the "id" payment.
     *
     * @param id the id of the entity.
     */
    void delete(Long id);
}
