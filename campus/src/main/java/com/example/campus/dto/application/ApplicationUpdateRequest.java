package com.example.campus.dto.application;

import jakarta.validation.constraints.Size;

public record ApplicationUpdateRequest(
        @Size(max = 20, message = "状态不能超过20个字符")
        String status,
        @Size(max = 500, message = "反馈内容不能超过500个字符")
        String decisionNote) {
}
