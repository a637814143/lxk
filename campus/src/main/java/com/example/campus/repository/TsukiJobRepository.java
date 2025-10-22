package com.example.campus.repository;

import com.example.campus.entity.TsukiJob;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiJobRepository extends JpaRepository<TsukiJob, Long> {

    List<TsukiJob> findByCompany_Id(Long companyId);
}
