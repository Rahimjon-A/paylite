package com.paylite.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class P2POperationTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static P2POperation getP2POperationSample1() {
        return new P2POperation()
            .id(1L)
            .requestId("requestId1")
            .amount(1L)
            .commissionAmount(1L)
            .totalAmount(1L)
            .fromPan("fromPan1")
            .toPan("toPan1")
            .failureReason("failureReason1");
    }

    public static P2POperation getP2POperationSample2() {
        return new P2POperation()
            .id(2L)
            .requestId("requestId2")
            .amount(2L)
            .commissionAmount(2L)
            .totalAmount(2L)
            .fromPan("fromPan2")
            .toPan("toPan2")
            .failureReason("failureReason2");
    }

    public static P2POperation getP2POperationRandomSampleGenerator() {
        return new P2POperation()
            .id(longCount.incrementAndGet())
            .requestId(UUID.randomUUID().toString())
            .amount(longCount.incrementAndGet())
            .commissionAmount(longCount.incrementAndGet())
            .totalAmount(longCount.incrementAndGet())
            .fromPan(UUID.randomUUID().toString())
            .toPan(UUID.randomUUID().toString())
            .failureReason(UUID.randomUUID().toString());
    }
}
