package com.example.campus.dto.company;

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
        String auditStatus,
        String auditReason) {
}
