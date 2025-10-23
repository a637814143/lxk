package com.example.campus.dto.discussion;

import java.time.LocalDateTime;

public record DiscussionResponse(
        Long id,
        Long companyId,
        String companyName,
        Long authorId,
        String authorName,
        String title,
        String content,
        String sanitizedContent,
        String status,
        String reviewComment,
        Long reviewerId,
        String reviewerName,
        LocalDateTime reviewTime,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
