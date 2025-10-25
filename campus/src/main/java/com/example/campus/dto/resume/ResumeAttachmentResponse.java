package com.example.campus.dto.resume;

import java.time.LocalDateTime;

public record ResumeAttachmentResponse(
        Long id,
        String fileName,
        String fileUrl,
        String contentType,
        Long fileSize,
        LocalDateTime uploadedAt) {
}
