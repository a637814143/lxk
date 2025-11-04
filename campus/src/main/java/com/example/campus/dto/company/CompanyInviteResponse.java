package com.example.campus.dto.company;

import java.time.LocalDateTime;

public record CompanyInviteResponse(
        Long id,
        String code,
        Long adminId,
        String adminName,
        String status,
        String note,
        String companyNameHint,
        LocalDateTime createdTime,
        LocalDateTime usedTime) {
}
