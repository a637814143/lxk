package com.example.campus.dto.application;

import java.time.LocalDateTime;

public record ApplicationResponse(
        Long id,
        Long studentId,
        Long resumeId,
        Long jobId,
        Long companyId,
        String status,
        String decisionNote,
        LocalDateTime applyTime,
        LocalDateTime updateTime) {
}
