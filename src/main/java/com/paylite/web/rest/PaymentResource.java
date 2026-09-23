package com.paylite.web.rest;

import com.paylite.domain.Payment;
import com.paylite.domain.dto.CreatePaymentRequest;
import com.paylite.domain.dto.ErrorResponse;
import com.paylite.domain.dto.PaymentResponse;
import com.paylite.domain.enumeration.PaymentStatus;
import com.paylite.mapper.PaymentMapper;
import com.paylite.service.PaymentService;
import jakarta.validation.Valid;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class PaymentResource {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentResource.class);

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    public PaymentResource(PaymentService paymentService, PaymentMapper paymentMapper) {
        this.paymentService = paymentService;
        this.paymentMapper = paymentMapper;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<?> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        LOG.debug("REST request to create Payment : account={}, amount={}", request.account(), request.amount());

        Payment paymentRequest = paymentMapper.toEntity(request);
        Payment payment = paymentService.createPayment(paymentRequest);

        if (payment.getStatus() == PaymentStatus.FAILED) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("INSUFFICIENT_BALANCE", "Insufficient agent balance"));
        }

        return ResponseEntity.ok(paymentMapper.toResponse(payment));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<Page<PaymentResponse>> getCurrentAgentPayments(Pageable pageable) {
        LOG.debug("REST request to get payments for current Agent");

        Page<PaymentResponse> response = paymentService.getCurrentAgentPayments(pageable).map(paymentMapper::toResponse);

        return ResponseEntity.ok(response);
    }
}
