package com.example.campus.repository;

import com.example.campus.entity.TsukiAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiAuditLogRepository extends JpaRepository<TsukiAuditLog, Long> {
}
