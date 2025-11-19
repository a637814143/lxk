package com.example.campus.dto.discussion;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record DiscussionCommentCreateRequest(
        @NotNull(message = "帖子ID不能为空")
        Long postId,

        @NotBlank(message = "评论内容不能为空")
        String content) {
}

