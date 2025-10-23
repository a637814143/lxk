package com.example.campus.dto.audit;

import java.time.LocalDateTime;

public record AuditLogResponse(
        Long id,
        Long adminId,
        String action,
        String targetType,
        Long targetId,
        String result,
        LocalDateTime timestamp) {
}
