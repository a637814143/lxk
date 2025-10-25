package com.example.campus.repository;

import com.example.campus.entity.TsukiResumeAttachment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiResumeAttachmentRepository extends JpaRepository<TsukiResumeAttachment, Long> {

    Optional<TsukiResumeAttachment> findByFilePath(String filePath);

    List<TsukiResumeAttachment> findByResume_Id(Long resumeId);

    List<TsukiResumeAttachment> findByStudent_IdAndResumeIsNull(Long studentId);
}
