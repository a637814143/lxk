package com.example.campus.repository;

import com.example.campus.entity.TsukiResume;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiResumeRepository extends JpaRepository<TsukiResume, Long> {

    List<TsukiResume> findByStudent_Id(Long studentId);
}
