package com.example.campus.repository;

import com.example.campus.entity.TsukiApplication;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiApplicationRepository extends JpaRepository<TsukiApplication, Long> {

    List<TsukiApplication> findByStudent_Id(Long studentId);

    List<TsukiApplication> findByCompany_Id(Long companyId);

    Optional<TsukiApplication> findByStudent_IdAndJob_Id(Long studentId, Long jobId);

    long countByStatus(String status);
}
