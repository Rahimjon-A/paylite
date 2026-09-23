package com.paylite.service;

import com.paylite.domain.Payment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.paylite.domain.Payment}.
 */
public interface PaymentService {
    Payment createPayment(Payment payment);

    Page<Payment> getCurrentAgentPayments(Pageable pageable);
}
