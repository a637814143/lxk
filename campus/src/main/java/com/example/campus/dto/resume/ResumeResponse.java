package com.example.campus.dto.resume;

import com.example.campus.dto.resume.ResumeAttachmentResponse;
import java.time.LocalDateTime;
import java.util.List;

public record ResumeResponse(
        Long id,
        Long studentId,
        String title,
        String educationExperience,
        String workExperience,
        String skills,
        String selfEvaluation,
        String attachment,
        List<ResumeAttachmentResponse> attachments,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
