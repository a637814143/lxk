package com.example.campus.dto.application;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ApplicationCreateRequest(
        @NotNull(message = "学生ID不能为空")
        Long studentId,

        @NotNull(message = "简历ID不能为空")
        Long resumeId,

        @NotNull(message = "职位ID不能为空")
        Long jobId,

        @NotNull(message = "企业ID不能为空")
        Long companyId,

        @Size(max = 20, message = "状态不能超过20个字符")
        String status) {
}
