package com.example.campus.dto.discussion;

import jakarta.validation.constraints.NotBlank;

public record DiscussionCreateRequest(
        Long companyId,

        String title,

        @NotBlank(message = "讨论内容不能为空")
        String content,

        Long parentId) {
}
