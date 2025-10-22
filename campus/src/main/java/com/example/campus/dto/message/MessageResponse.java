package com.example.campus.dto.message;

import java.time.LocalDateTime;

public record MessageResponse(
        Long id,
        Long senderId,
        Long receiverId,
        String title,
        String content,
        Boolean isRead,
        LocalDateTime sendTime) {
}
