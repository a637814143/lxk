package com.example.campus.repository;

import com.example.campus.entity.TsukiResumeUpload;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiResumeUploadRepository extends JpaRepository<TsukiResumeUpload, Long> {

    Optional<TsukiResumeUpload> findTopByStudent_IdAndFilePathOrderByUploadTimeDesc(Long studentId, String filePath);
}
