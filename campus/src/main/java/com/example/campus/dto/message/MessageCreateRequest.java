package com.example.campus.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MessageCreateRequest(
        Long senderId,

        @NotNull(message = "接收者ID不能为空")
        Long receiverId,

        @NotBlank(message = "消息标题不能为空")
        @Size(max = 100, message = "消息标题不能超过100个字符")
        String title,

        @NotBlank(message = "消息内容不能为空")
        String content,

        Boolean isRead) {
}
