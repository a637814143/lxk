package com.example.campus.dto.discussion;

import jakarta.validation.constraints.NotBlank;

public record DiscussionReviewRequest(
        @NotBlank(message = "审核状态不能为空")
        String status,

        String comment) {
}
