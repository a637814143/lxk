package com.example.campus.controller;

import com.example.campus.dto.audit.AuditLogCreateRequest;
import com.example.campus.dto.audit.AuditLogResponse;
import com.example.campus.dto.audit.AuditLogUpdateRequest;
import com.example.campus.service.AuditLogService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping
    public List<AuditLogResponse> list() {
        return auditLogService.findAll();
    }

    @GetMapping("/{id}")
    public AuditLogResponse get(@PathVariable Long id) {
        return auditLogService.findById(id);
    }

    @PostMapping
    public ResponseEntity<AuditLogResponse> create(@Valid @RequestBody AuditLogCreateRequest request) {
        AuditLogResponse response = auditLogService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public AuditLogResponse update(@PathVariable Long id, @Valid @RequestBody AuditLogUpdateRequest request) {
        return auditLogService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        auditLogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
