package com.example.campus.dto.portal.student;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record StudentResumeRequest(
        @NotBlank(message = "简历标题不能为空")
        @Size(max = 100, message = "简历标题不能超过100个字符")
        String title,
        String educationExperience,
        String workExperience,
        String skills,
        String selfEvaluation,
        @Size(max = 255, message = "附件链接不能超过255个字符")
        String attachment,
        Long attachmentId) {
}
