package com.example.campus.service;

import com.example.campus.dto.audit.AuditLogCreateRequest;
import com.example.campus.dto.audit.AuditLogResponse;
import com.example.campus.dto.audit.AuditLogUpdateRequest;
import com.example.campus.entity.TsukiAdmin;
import com.example.campus.entity.TsukiAuditLog;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiAdminRepository;
import com.example.campus.repository.TsukiAuditLogRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final TsukiAuditLogRepository auditLogRepository;
    private final TsukiAdminRepository adminRepository;

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findAll() {
        return auditLogRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findByAdminId(Long adminId) {
        return auditLogRepository.findByAdmin_Id(adminId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AuditLogResponse findById(Long id) {
        return toResponse(getLog(id));
    }

    @Transactional
    public AuditLogResponse create(AuditLogCreateRequest request) {
        TsukiAdmin admin = getAdmin(request.adminId());
        TsukiAuditLog log = TsukiAuditLog.builder()
                .admin(admin)
                .action(request.action())
                .targetType(request.targetType())
                .targetId(request.targetId())
                .result(request.result())
                .build();
        TsukiAuditLog saved = auditLogRepository.save(log);
        return toResponse(saved);
    }

    @Transactional
    public AuditLogResponse update(Long id, AuditLogUpdateRequest request) {
        TsukiAuditLog log = getLog(id);
        if (request.action() != null) {
            log.setAction(request.action());
        }
        if (request.targetType() != null) {
            log.setTargetType(request.targetType());
        }
        if (request.targetId() != null) {
            log.setTargetId(request.targetId());
        }
        if (request.result() != null) {
            log.setResult(request.result());
        }
        return toResponse(log);
    }

    @Transactional
    public void delete(Long id) {
        TsukiAuditLog log = getLog(id);
        auditLogRepository.delete(log);
    }

    private TsukiAuditLog getLog(Long id) {
        return auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的操作日志"));
    }

    private TsukiAdmin getAdmin(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的管理员"));
    }

    private AuditLogResponse toResponse(TsukiAuditLog log) {
        return new AuditLogResponse(
                log.getId(),
                log.getAdmin().getId(),
                log.getAction(),
                log.getTargetType(),
                log.getTargetId(),
                log.getResult(),
                log.getTimestamp());
    }
}
