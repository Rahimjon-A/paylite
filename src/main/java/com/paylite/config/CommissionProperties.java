package com.paylite.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "paylite.commission")
public class CommissionProperties {

    private BigDecimal uzcardToUzcard;
    private BigDecimal uzcardToHumo;
    private BigDecimal humoToUzcard;
    private BigDecimal humoToHumo;

    public BigDecimal getUzcardToUzcard() {
        return uzcardToUzcard;
    }

    public void setUzcardToUzcard(BigDecimal uzcardToUzcard) {
        this.uzcardToUzcard = uzcardToUzcard;
    }

    public BigDecimal getUzcardToHumo() {
        return uzcardToHumo;
    }

    public void setUzcardToHumo(BigDecimal uzcardToHumo) {
        this.uzcardToHumo = uzcardToHumo;
    }

    public BigDecimal getHumoToUzcard() {
        return humoToUzcard;
    }

    public void setHumoToUzcard(BigDecimal humoToUzcard) {
        this.humoToUzcard = humoToUzcard;
    }

    public BigDecimal getHumoToHumo() {
        return humoToHumo;
    }

    public void setHumoToHumo(BigDecimal humoToHumo) {
        this.humoToHumo = humoToHumo;
    }
}
