package com.example.campus.dto.announcement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AnnouncementCreateRequest(
        @NotNull(message = "管理员ID不能为空")
        Long adminId,

        @NotBlank(message = "公告标题不能为空")
        @Size(max = 100, message = "公告标题不能超过100个字符")
        String title,

        @NotBlank(message = "公告内容不能为空")
        String content,

        @Size(max = 20, message = "公告目标不能超过20个字符")
        String target) {
}
