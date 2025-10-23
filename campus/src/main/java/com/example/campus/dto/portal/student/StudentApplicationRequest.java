package com.example.campus.dto.portal.student;

import jakarta.validation.constraints.NotNull;

public record StudentApplicationRequest(
        @NotNull(message = "职位ID不能为空")
        Long jobId,

        @NotNull(message = "简历ID不能为空")
        Long resumeId) {
}
