package com.example.campus.dto.portal.admin;

import jakarta.validation.constraints.NotBlank;

public record JobAuditRequest(
        @NotBlank(message = "审核状态不能为空")
        String status,
        String reason) {
}
