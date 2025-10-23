package com.example.campus.dto.announcement;

import java.time.LocalDateTime;

public record AnnouncementResponse(
        Long id,
        Long adminId,
        String title,
        String content,
        String target,
        LocalDateTime publishTime) {
}
