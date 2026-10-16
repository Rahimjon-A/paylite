package com.paylite.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

public class CommissionLedgerTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    public static CommissionLedger getCommissionLedgerSample1() {
        return new CommissionLedger()
            .id(1L)
            .requestId("requestId1")
            .fromPan("fromPan1")
            .destinationAccount("destinationAccount1")
            .totalAmount(1L)
            .transferredAmount(1L)
            .commissionAmount(1L);
    }

    public static CommissionLedger getCommissionLedgerSample2() {
        return new CommissionLedger()
            .id(2L)
            .requestId("requestId2")
            .fromPan("fromPan2")
            .destinationAccount("destinationAccount2")
            .totalAmount(2L)
            .transferredAmount(2L)
            .commissionAmount(2L);
    }

    public static CommissionLedger getCommissionLedgerRandomSampleGenerator() {
        return new CommissionLedger()
            .id(longCount.incrementAndGet())
            .requestId(UUID.randomUUID().toString())
            .fromPan(UUID.randomUUID().toString())
            .destinationAccount(UUID.randomUUID().toString())
            .totalAmount(longCount.incrementAndGet())
            .transferredAmount(longCount.incrementAndGet())
            .commissionAmount(longCount.incrementAndGet());
    }
}
