package com.example.campus.dto.portal.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ApplicationStatusRequest(
        @NotBlank(message = "投递状态不能为空")
        String status,
        @Size(max = 500, message = "反馈说明不能超过500个字符")
        String decisionNote,
        Boolean notifyStudent) {
}
