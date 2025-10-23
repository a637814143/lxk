package com.example.campus.dto.message;

import jakarta.validation.constraints.Size;

public record MessageUpdateRequest(
        @Size(max = 100, message = "消息标题不能超过100个字符")
        String title,

        String content,

        Boolean isRead) {
}
