package com.paylite.domain;

import static com.paylite.domain.CommissionLedgerTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.paylite.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CommissionLedgerTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(CommissionLedger.class);
        CommissionLedger commissionLedger1 = getCommissionLedgerSample1();
        CommissionLedger commissionLedger2 = new CommissionLedger();
        assertThat(commissionLedger1).isNotEqualTo(commissionLedger2);

        commissionLedger2.setId(commissionLedger1.getId());
        assertThat(commissionLedger1).isEqualTo(commissionLedger2);

        commissionLedger2 = getCommissionLedgerSample2();
        assertThat(commissionLedger1).isNotEqualTo(commissionLedger2);
    }
}
