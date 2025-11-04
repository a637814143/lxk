package com.example.campus.service;

import com.example.campus.entity.TsukiResume;
import com.example.campus.entity.TsukiResumeAttachment;
import com.example.campus.entity.TsukiResumeUpload;
import com.example.campus.entity.TsukiStudent;
import com.example.campus.repository.TsukiResumeAttachmentRepository;
import com.example.campus.repository.TsukiResumeUploadRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ResumeAttachmentService {

    private final TsukiResumeAttachmentRepository attachmentRepository;
    private final TsukiResumeUploadRepository uploadRepository;

    @Transactional
    public void recordUpload(TsukiStudent student, MultipartFile file, String publicPath) {
        TsukiResumeUpload upload = TsukiResumeUpload.builder()
                .student(student)
                .fileName(resolveOriginalFilename(file))
                .filePath(publicPath)
                .fileSize(file != null ? file.getSize() : null)
                .visibility("company_only")
                .build();
        uploadRepository.save(upload);
    }

    @Transactional
    public void syncAttachment(TsukiStudent student, TsukiResume resume, String attachmentPath, MultipartFile file) {
        attachmentRepository.deleteAllByResume_Id(resume.getId());
        if (!StringUtils.hasText(attachmentPath)) {
            return;
        }
        String fileName = null;
        Long fileSize = null;
        String contentType = null;
        LocalDateTime uploadedAt = null;

        if (file != null) {
            fileName = resolveOriginalFilename(file);
            contentType = file.getContentType();
            fileSize = file.getSize();
            uploadedAt = LocalDateTime.now();
        } else {
            Optional<TsukiResumeUpload> uploadOptional = uploadRepository
                    .findTopByStudent_IdAndFilePathOrderByUploadTimeDesc(student.getId(), attachmentPath);
            if (uploadOptional.isPresent()) {
                TsukiResumeUpload upload = uploadOptional.get();
                fileName = upload.getFileName();
                fileSize = upload.getFileSize();
                uploadedAt = upload.getUploadTime();
            }
        }

        if (!StringUtils.hasText(fileName)) {
            fileName = extractFileName(attachmentPath);
        }
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }

        TsukiResumeAttachment attachment = TsukiResumeAttachment.builder()
                .student(student)
                .resume(resume)
                .fileName(fileName)
                .filePath(attachmentPath)
                .contentType(contentType)
                .fileSize(fileSize)
                .uploadedAt(uploadedAt)
                .build();
        attachmentRepository.save(attachment);
    }

    @Transactional
    public void deleteByResume(TsukiResume resume) {
        attachmentRepository.deleteAllByResume_Id(resume.getId());
    }

    private String resolveOriginalFilename(MultipartFile file) {
        if (file == null || file.getOriginalFilename() == null) {
            return "resume";
        }
        return StringUtils.cleanPath(file.getOriginalFilename());
    }

    private String extractFileName(String path) {
        String filename = StringUtils.getFilename(path);
        return filename != null ? filename : "attachment";
    }
}
