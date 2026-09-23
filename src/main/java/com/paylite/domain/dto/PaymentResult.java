package com.paylite.domain.dto;

public record PaymentResult(PaymentResponse payment, boolean insufficientBalance) {}
