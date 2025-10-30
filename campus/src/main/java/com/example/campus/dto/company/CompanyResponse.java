package com.example.campus.dto.company;

import java.math.BigDecimal;

public record CompanyResponse(
        Long id,
        Long userId,
        String companyName,
        String licenseNumber,
        String industry,
        String address,
        String website,
        String description,
        String logo,
        String licenseDocument,
        String auditStatus,
        String auditReason,
        BigDecimal walletBalance) {
}
