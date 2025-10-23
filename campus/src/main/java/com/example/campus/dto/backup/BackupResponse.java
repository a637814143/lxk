package com.example.campus.dto.backup;

import java.time.LocalDateTime;

public record BackupResponse(
        Long id,
        Long adminId,
        String adminName,
        String filePath,
        String downloadUrl,
        String backupType,
        String status,
        Long sizeBytes,
        String message,
        LocalDateTime createdAt) {
}
