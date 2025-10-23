package com.example.campus.repository;

import com.example.campus.entity.TsukiCompany;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiCompanyRepository extends JpaRepository<TsukiCompany, Long> {

    Optional<TsukiCompany> findByUser_Id(Long userId);

    List<TsukiCompany> findByAuditStatus(String auditStatus);

    long countByAuditStatus(String auditStatus);
}
