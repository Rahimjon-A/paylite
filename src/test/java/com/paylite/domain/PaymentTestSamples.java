package com.paylite.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class PaymentTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static Payment getPaymentSample1() {
        return new Payment().id(1L).accountNumber("accountNumber1").amount(1L).commissionAmount(1L).totalAmount(1L);
    }

    public static Payment getPaymentSample2() {
        return new Payment().id(2L).accountNumber("accountNumber2").amount(2L).commissionAmount(2L).totalAmount(2L);
    }

    public static Payment getPaymentRandomSampleGenerator() {
        return new Payment()
            .id(longCount.incrementAndGet())
            .accountNumber(UUID.randomUUID().toString())
            .amount(longCount.incrementAndGet())
            .commissionAmount(longCount.incrementAndGet())
            .totalAmount(longCount.incrementAndGet());
    }
}
