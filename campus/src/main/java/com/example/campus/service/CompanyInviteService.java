package com.example.campus.service;

import com.example.campus.dto.company.CompanyInviteCreateRequest;
import com.example.campus.dto.company.CompanyInviteResponse;
import com.example.campus.dto.company.CompanyInviteStatusRequest;
import com.example.campus.entity.TsukiAdmin;
import com.example.campus.entity.TsukiCompanyInvite;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiAdminRepository;
import com.example.campus.repository.TsukiCompanyInviteRepository;
import org.springframework.transaction.annotation.Transactional;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CompanyInviteService {

    private static final String CODE_CHARSET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int DEFAULT_CODE_LENGTH = 12;
    private static final SecureRandom RANDOM = new SecureRandom();
    private static final Set<String> MUTABLE_STATUSES = Set.of("active", "revoked");

    private final TsukiCompanyInviteRepository inviteRepository;
    private final TsukiAdminRepository adminRepository;

    @Transactional
    public CompanyInviteResponse create(Long adminUserId, CompanyInviteCreateRequest request) {
        TsukiAdmin admin = requireAdmin(adminUserId);
        String code = generateUniqueCode();
        TsukiCompanyInvite invite = TsukiCompanyInvite.builder()
                .admin(admin)
                .code(code)
                .note(trimToNull(request.note()))
                .companyNameHint(trimToNull(request.companyNameHint()))
                .status("active")
                .build();
        return toResponse(inviteRepository.save(invite));
    }

    @Transactional
    public CompanyInviteResponse updateStatus(Long adminUserId, Long inviteId, CompanyInviteStatusRequest request) {
        requireAdmin(adminUserId);
        TsukiCompanyInvite invite = inviteRepository.findById(inviteId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到邀请码"));
        if ("used".equalsIgnoreCase(invite.getStatus())) {
            throw new IllegalArgumentException("邀请码已被使用，无法修改状态");
        }
        String status = normalizeStatus(request.status());
        if (!MUTABLE_STATUSES.contains(status)) {
            throw new IllegalArgumentException("状态仅支持 active 或 revoked");
        }
        invite.setStatus(status);
        if ("active".equals(status)) {
            invite.setUsedTime(null);
        }
        return toResponse(inviteRepository.save(invite));
    }

    @Transactional
    public TsukiCompanyInvite consumeInvite(String inviteCode, String companyName) {
        if (!StringUtils.hasText(inviteCode)) {
            throw new IllegalArgumentException("请输入有效的邀请码");
        }
        String normalizedCode = inviteCode.trim().toUpperCase(Locale.ROOT);
        TsukiCompanyInvite invite = inviteRepository.findByCodeIgnoreCase(normalizedCode)
                .orElseThrow(() -> new IllegalArgumentException("邀请码不存在或已失效"));
        if (!"active".equalsIgnoreCase(invite.getStatus())) {
            throw new IllegalArgumentException("邀请码已被使用或已失效，请联系管理员");
        }
        invite.setStatus("used");
        invite.setUsedTime(LocalDateTime.now());
        if (!StringUtils.hasText(invite.getCompanyNameHint()) && StringUtils.hasText(companyName)) {
            invite.setCompanyNameHint(companyName.trim());
        }
        return inviteRepository.save(invite);
    }

    @Transactional
    public List<CompanyInviteResponse> listAll() {
        return inviteRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private String generateUniqueCode() {
        for (int attempts = 0; attempts < 10_000; attempts++) {
            String candidate = randomCode();
            if (inviteRepository.findByCodeIgnoreCase(candidate).isEmpty()) {
                return candidate;
            }
        }
        throw new IllegalStateException("无法生成唯一的邀请码，请稍后再试");
    }

    private String randomCode() {
        StringBuilder builder = new StringBuilder(DEFAULT_CODE_LENGTH);
        for (int i = 0; i < DEFAULT_CODE_LENGTH; i++) {
            int index = RANDOM.nextInt(CODE_CHARSET.length());
            builder.append(CODE_CHARSET.charAt(index));
        }
        return builder.toString();
    }

    private TsukiAdmin requireAdmin(Long adminUserId) {
        return adminRepository.findByUser_Id(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("当前账号不是系统管理员"));
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("状态不能为空");
        }
        return status.trim().toLowerCase(Locale.ROOT);
    }

    private CompanyInviteResponse toResponse(TsukiCompanyInvite invite) {
        return new CompanyInviteResponse(
                invite.getId(),
                invite.getCode(),
                invite.getAdmin() != null ? invite.getAdmin().getId() : null,
                invite.getAdmin() != null ? invite.getAdmin().getName() : null,
                invite.getStatus(),
                invite.getNote(),
                invite.getCompanyNameHint(),
                invite.getCreatedTime(),
                invite.getUsedTime());
    }
}
