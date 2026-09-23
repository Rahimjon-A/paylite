package com.paylite.service;

import com.paylite.domain.dto.CreatePaymentRequest;
import com.paylite.domain.dto.PaymentResponse;
import com.paylite.domain.dto.PaymentResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service Interface for managing {@link com.paylite.domain.Payment}.
 */
public interface PaymentService {
    PaymentResult createPayment(CreatePaymentRequest request);
    Page<PaymentResponse> getCurrentAgentPayments(Pageable pageable);
}
