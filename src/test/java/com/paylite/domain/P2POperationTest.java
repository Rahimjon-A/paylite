package com.paylite.domain;

import static com.paylite.domain.P2POperationTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import com.paylite.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class P2POperationTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(P2POperation.class);
        P2POperation p2POperation1 = getP2POperationSample1();
        P2POperation p2POperation2 = new P2POperation();
        assertThat(p2POperation1).isNotEqualTo(p2POperation2);

        p2POperation2.setId(p2POperation1.getId());
        assertThat(p2POperation1).isEqualTo(p2POperation2);

        p2POperation2 = getP2POperationSample2();
        assertThat(p2POperation1).isNotEqualTo(p2POperation2);
    }
}
