package com.example.campus.controller;

import com.example.campus.config.BillingProperties;
import com.example.campus.dto.billing.BillingConfigResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/billing")
@RequiredArgsConstructor
public class BillingController {

    private final BillingProperties billingProperties;

    @GetMapping("/config")
    public BillingConfigResponse getConfig() {
        return new BillingConfigResponse(billingProperties.getQuarterFee());
    }
}
