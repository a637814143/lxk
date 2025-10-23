package com.example.campus.dto.application;

import jakarta.validation.constraints.Size;

public record ApplicationUpdateRequest(
        @Size(max = 20, message = "状态不能超过20个字符")
        String status) {
}
