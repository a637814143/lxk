package com.example.campus.dto.discussion;

import java.time.LocalDateTime;

public record DiscussionCommentResponse(
        Long id,
        Long postId,
        Long authorUserId,
        String authorUsername,
        String content,
        String sanitizedContent,
        String status,
        String reviewComment,
        Long reviewerAdminId,
        String reviewerUsername,
        LocalDateTime reviewTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}

