package com.paylite.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Paylite.
 * <p>
 * Properties are configured in the {@code application.yml} file.
 * See {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

    private final Liquibase liquibase = new Liquibase();
    private final Payment payment = new Payment();

    // jhipster-needle-application-properties-property

    public Liquibase getLiquibase() {
        return liquibase;
    }

    public Payment getPayment() {
        return payment;
    }

    // jhipster-needle-application-properties-property-getter

    public static class Liquibase {

        private Boolean asyncStart = true;

        public Boolean getAsyncStart() {
            return asyncStart;
        }

        public void setAsyncStart(Boolean asyncStart) {
            this.asyncStart = asyncStart;
        }
    }

    public static class Payment {

        private BigDecimal commissionPercent;

        public BigDecimal getCommissionPercent() {
            return commissionPercent;
        }

        public void setCommissionPercent(BigDecimal commissionPercent) {
            this.commissionPercent = commissionPercent;
        }
    }
    // jhipster-needle-application-properties-property-class
}
