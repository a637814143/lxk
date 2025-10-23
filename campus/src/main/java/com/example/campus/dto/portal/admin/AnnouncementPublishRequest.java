package com.example.campus.dto.portal.admin;

import jakarta.validation.constraints.NotBlank;

public record AnnouncementPublishRequest(
        @NotBlank(message = "公告标题不能为空")
        String title,
        @NotBlank(message = "公告内容不能为空")
        String content,
        @NotBlank(message = "公告目标不能为空")
        String target) {
}
