package com.example.campus.dto.discussion;

import jakarta.validation.constraints.NotBlank;

public record DiscussionCreateRequest(
        @NotBlank(message = "讨论标题不能为空")
        String title,

        @NotBlank(message = "讨论内容不能为空")
        String content) {
}
