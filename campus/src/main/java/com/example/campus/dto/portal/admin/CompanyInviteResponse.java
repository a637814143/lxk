package com.example.campus.dto.portal.admin;

import java.time.LocalDateTime;

public record CompanyInviteResponse(
        Long id,
        String code,
        String status,
        String note,
        String companyNameHint,
        Long adminId,
        String adminName,
        LocalDateTime createdTime,
        LocalDateTime usedTime) {
}
