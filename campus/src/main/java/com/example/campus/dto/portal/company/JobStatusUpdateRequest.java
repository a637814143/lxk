package com.example.campus.dto.portal.company;

import jakarta.validation.constraints.NotBlank;

public record JobStatusUpdateRequest(
        @NotBlank(message = "职位状态不能为空")
        String status) {
}
