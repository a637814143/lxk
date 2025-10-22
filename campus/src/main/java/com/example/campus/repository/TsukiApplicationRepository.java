package com.example.campus.repository;

import com.example.campus.entity.TsukiApplication;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiApplicationRepository extends JpaRepository<TsukiApplication, Long> {

    List<TsukiApplication> findByStudent_Id(Long studentId);

    List<TsukiApplication> findByCompany_Id(Long companyId);
}
