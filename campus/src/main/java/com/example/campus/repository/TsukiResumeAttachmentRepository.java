package com.example.campus.repository;

import com.example.campus.entity.TsukiResumeAttachment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiResumeAttachmentRepository extends JpaRepository<TsukiResumeAttachment, Long> {

    List<TsukiResumeAttachment> findByResume_Id(Long resumeId);

    void deleteAllByResume_Id(Long resumeId);

    Optional<TsukiResumeAttachment> findTopByStudent_IdAndFilePathOrderByUploadedAtDesc(Long studentId, String filePath);
}
