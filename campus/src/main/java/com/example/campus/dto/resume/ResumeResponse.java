package com.example.campus.dto.resume;

import java.time.LocalDateTime;

public record ResumeResponse(
        Long id,
        Long studentId,
        String title,
        String educationExperience,
        String workExperience,
        String skills,
        String selfEvaluation,
        String attachment,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
