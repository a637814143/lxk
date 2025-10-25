package com.example.campus.service;

import com.example.campus.dto.company.CompanyCreateRequest;
import com.example.campus.dto.company.CompanyResponse;
import com.example.campus.dto.company.CompanyUpdateRequest;
import com.example.campus.entity.TsukiCompany;
import com.example.campus.entity.TsukiUser;
import com.example.campus.entity.UserRole;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiCompanyRepository;
import com.example.campus.repository.TsukiUserRepository;
import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("pending", "approved", "rejected");

    private final TsukiCompanyRepository companyRepository;
    private final TsukiUserRepository userRepository;

    @Transactional(readOnly = true)
    public List<CompanyResponse> findAll() {
        return companyRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<CompanyResponse> findByUserId(Long userId) {
        return companyRepository.findByUser_Id(userId)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CompanyResponse findById(Long id) {
        return toResponse(getCompany(id));
    }

    @Transactional
    public CompanyResponse create(CompanyCreateRequest request) {
        TsukiUser user = getUser(request.userId(), UserRole.COMPANY);
        TsukiCompany company = TsukiCompany.builder()
                .user(user)
                .companyName(request.companyName())
                .licenseNumber(request.licenseNumber())
                .industry(request.industry())
                .address(request.address())
                .website(request.website())
                .description(request.description())
                .logo(request.logo())
                .licenseDocument(request.licenseDocument())
                .auditStatus(normalizeStatus(request.auditStatus()))
                .auditReason(request.auditReason())
                .build();
        TsukiCompany saved = companyRepository.save(company);
        return toResponse(saved);
    }

    @Transactional
    public CompanyResponse update(Long id, CompanyUpdateRequest request) {
        TsukiCompany company = getCompany(id);
        if (request.companyName() != null) {
            company.setCompanyName(request.companyName());
        }
        if (request.licenseNumber() != null) {
            company.setLicenseNumber(request.licenseNumber());
        }
        if (request.industry() != null) {
            company.setIndustry(request.industry());
        }
        if (request.address() != null) {
            company.setAddress(request.address());
        }
        if (request.website() != null) {
            company.setWebsite(request.website());
        }
        if (request.description() != null) {
            company.setDescription(request.description());
        }
        if (request.logo() != null) {
            company.setLogo(request.logo());
        }
        if (request.licenseDocument() != null) {
            company.setLicenseDocument(request.licenseDocument());
        }
        if (request.auditStatus() != null) {
            company.setAuditStatus(normalizeStatus(request.auditStatus()));
        }
        if (request.auditReason() != null) {
            company.setAuditReason(request.auditReason());
        }
        return toResponse(company);
    }

    @Transactional
    public void delete(Long id) {
        TsukiCompany company = getCompany(id);
        companyRepository.delete(company);
    }

    private TsukiCompany getCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的企业"));
    }

    private TsukiUser getUser(Long userId, UserRole requiredRole) {
        TsukiUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + userId + "的用户"));
        if (requiredRole != null && user.getRole() != requiredRole) {
            throw new IllegalArgumentException("用户角色必须为" + requiredRole.getDisplayName());
        }
        return user;
    }

    private CompanyResponse toResponse(TsukiCompany company) {
        return new CompanyResponse(
                company.getId(),
                company.getUser().getId(),
                company.getCompanyName(),
                company.getLicenseNumber(),
                company.getIndustry(),
                company.getAddress(),
                company.getWebsite(),
                company.getDescription(),
                company.getLogo(),
                company.getLicenseDocument(),
                company.getAuditStatus(),
                company.getAuditReason());
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "approved";
        }
        String normalized = status.toLowerCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("审核状态仅支持: " + String.join("/", ALLOWED_STATUSES));
        }
        return normalized;
    }
}
