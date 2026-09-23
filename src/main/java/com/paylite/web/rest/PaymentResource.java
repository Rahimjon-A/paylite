package com.paylite.web.rest;

import com.paylite.domain.Payment;
import com.paylite.domain.dto.CreatePaymentRequest;
import com.paylite.domain.dto.ErrorResponse;
import com.paylite.domain.dto.PaymentResponse;
import com.paylite.domain.dto.PaymentResult;
import com.paylite.repository.PaymentRepository;
import com.paylite.service.PaymentService;
import com.paylite.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link com.paylite.domain.Payment}.
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentResource {

    private static final Logger LOG = LoggerFactory.getLogger(PaymentResource.class);

    private final PaymentService paymentService;

    public PaymentResource(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<?> createPayment(@Valid @RequestBody CreatePaymentRequest request) {
        PaymentResult result = paymentService.createPayment(request);

        if (result.insufficientBalance()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("INSUFFICIENT_BALANCE", "Insufficient agent balance"));
        }

        return ResponseEntity.ok(result.payment());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_AGENT')")
    public ResponseEntity<Page<PaymentResponse>> getCurrentAgentPayments(@ParameterObject Pageable pageable) {
        LOG.debug("REST request to get a page of Agent Payments");
        return ResponseEntity.ok(paymentService.getCurrentAgentPayments(pageable));
    }
}
