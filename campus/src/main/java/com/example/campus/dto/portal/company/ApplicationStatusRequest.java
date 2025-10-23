package com.example.campus.dto.portal.company;

import jakarta.validation.constraints.NotBlank;

public record ApplicationStatusRequest(
        @NotBlank(message = "投递状态不能为空")
        String status,
        String messageTitle,
        String messageContent) {
}
