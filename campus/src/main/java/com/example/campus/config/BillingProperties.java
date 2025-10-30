package com.example.campus.config;

import java.math.BigDecimal;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.billing")
public class BillingProperties {

    private BigDecimal quarterFee = BigDecimal.ZERO;

    public BigDecimal getQuarterFee() {
        return quarterFee;
    }

    public void setQuarterFee(BigDecimal quarterFee) {
        this.quarterFee = quarterFee == null ? BigDecimal.ZERO : quarterFee;
    }
}
