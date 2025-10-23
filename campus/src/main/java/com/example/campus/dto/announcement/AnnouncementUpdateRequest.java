package com.example.campus.dto.announcement;

import jakarta.validation.constraints.Size;

public record AnnouncementUpdateRequest(
        @Size(max = 100, message = "公告标题不能超过100个字符")
        String title,

        String content,

        @Size(max = 20, message = "公告目标不能超过20个字符")
        String target) {
}
