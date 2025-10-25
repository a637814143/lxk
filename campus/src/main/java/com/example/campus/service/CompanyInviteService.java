package com.example.campus.service;

import com.example.campus.dto.portal.admin.CompanyInviteCreateRequest;
import com.example.campus.dto.portal.admin.CompanyInviteResponse;
import com.example.campus.entity.TsukiAdmin;
import com.example.campus.entity.TsukiCompanyInvite;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiAdminRepository;
import com.example.campus.repository.TsukiCompanyInviteRepository;
import jakarta.annotation.PostConstruct;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyInviteService {

    private static final String CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int DEFAULT_CODE_LENGTH = 10;

    private final TsukiCompanyInviteRepository inviteRepository;
    private final TsukiAdminRepository adminRepository;
    private final JdbcTemplate jdbcTemplate;
    private final SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    public void ensureTableExists() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS tsuki_company_invite (
                    invite_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                    code VARCHAR(64) NOT NULL UNIQUE,
                    admin_id BIGINT NOT NULL,
                    note VARCHAR(255),
                    company_name_hint VARCHAR(100),
                    status ENUM('active','used','revoked') NOT NULL DEFAULT 'active',
                    created_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                    used_time DATETIME NULL,
                    CONSTRAINT fk_invite_admin FOREIGN KEY (admin_id) REFERENCES tsuki_admin(admin_id) ON DELETE CASCADE
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业邀请码表';
                """);
    }

    @Transactional(readOnly = true)
    public long countActiveInvites() {
        return inviteRepository.countByStatus("active");
    }

    @Transactional(readOnly = true)
    public List<CompanyInviteResponse> listInvites() {
        return inviteRepository.findAllByOrderByCreatedTimeDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CompanyInviteResponse createInvite(Long adminUserId, CompanyInviteCreateRequest request) {
        TsukiAdmin admin = requireAdminByUserId(adminUserId);
        String normalizedCode = normalizeCode(request.code());
        if (normalizedCode != null && inviteRepository.findByCodeIgnoreCase(normalizedCode).isPresent()) {
            throw new IllegalArgumentException("邀请码已存在，请重新生成");
        }
        String code = normalizedCode != null ? normalizedCode : generateCode(DEFAULT_CODE_LENGTH);
        TsukiCompanyInvite invite = TsukiCompanyInvite.builder()
                .admin(admin)
                .code(code)
                .note(normalizeNullable(request.note()))
                .companyNameHint(normalizeNullable(request.companyNameHint()))
                .status("active")
                .build();
        TsukiCompanyInvite saved = inviteRepository.save(invite);
        return toResponse(saved);
    }

    @Transactional
    public CompanyInviteResponse revokeInvite(Long adminUserId, Long inviteId) {
        requireAdminByUserId(adminUserId);
        TsukiCompanyInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到对应的邀请码"));
        if (!"active".equals(invite.getStatus())) {
            throw new IllegalStateException("仅能撤销处于启用状态的邀请码");
        }
        invite.setStatus("revoked");
        invite.setUsedTime(LocalDateTime.now());
        return toResponse(invite);
    }

    @Transactional
    public void consumeInvite(String code, String companyName) {
        String normalizedCode = normalizeCode(code);
        if (normalizedCode == null) {
            throw new IllegalArgumentException("企业注册需要提供管理员发放的邀请码");
        }
        TsukiCompanyInvite invite = inviteRepository.findWithLockByCodeIgnoreCase(normalizedCode)
                .orElseThrow(() -> new IllegalArgumentException("邀请码无效，请联系管理员"));
        if (!"active".equals(invite.getStatus())) {
            throw new IllegalArgumentException("该邀请码已被使用或失效");
        }
        invite.setStatus("used");
        invite.setUsedTime(LocalDateTime.now());
        if (companyName != null && !companyName.isBlank()) {
            invite.setCompanyNameHint(companyName);
        }
    }

    private CompanyInviteResponse toResponse(TsukiCompanyInvite invite) {
        return new CompanyInviteResponse(
                invite.getId(),
                invite.getCode(),
                invite.getStatus(),
                invite.getNote(),
                invite.getCompanyNameHint(),
                invite.getAdmin() != null ? invite.getAdmin().getId() : null,
                invite.getAdmin() != null ? invite.getAdmin().getName() : null,
                invite.getCreatedTime(),
                invite.getUsedTime());
    }

    private TsukiAdmin requireAdminByUserId(Long adminUserId) {
        return adminRepository.findByUser_Id(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("仅系统管理员可以操作邀请码"));
    }

    private String generateCode(int length) {
        StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            builder.append(CHARSET.charAt(secureRandom.nextInt(CHARSET.length())));
        }
        return builder.toString();
    }

    private String normalizeCode(String code) {
        if (code == null) {
            return null;
        }
        String trimmed = code.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.toUpperCase(Locale.ROOT);
    }

    private String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
