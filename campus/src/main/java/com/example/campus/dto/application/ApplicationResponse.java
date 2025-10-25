package com.example.campus.dto.application;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long studentId,
        Long resumeId,
        Long jobId,
        Long companyId,
        String status,
        LocalDateTime applyTime,
        LocalDateTime updateTime,
        String studentName,
        String companyName,
        String jobTitle,
        ResumeSnapshot resume,
        String decisionNote) {

    public record ResumeSnapshot(
            String title,
            String educationExperience,
            String workExperience,
            String skills,
            String selfEvaluation,
            String attachment) {
    }
}
