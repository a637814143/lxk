package com.example.campus.dto.audit;

import jakarta.validation.constraints.Size;

public record AuditLogUpdateRequest(
        @Size(max = 100, message = "操作行为不能超过100个字符")
        String action,

        @Size(max = 50, message = "目标类型不能超过50个字符")
        String targetType,

        Long targetId,

        @Size(max = 255, message = "操作结果不能超过255个字符")
        String result) {
}
