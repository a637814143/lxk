package com.example.campus.dto.backup;

public record BackupCreateRequest(
        String backupType,
        String message) {
}
