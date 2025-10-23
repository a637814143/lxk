package com.example.campus.service;

import com.example.campus.dto.announcement.AnnouncementCreateRequest;
import com.example.campus.dto.announcement.AnnouncementResponse;
import com.example.campus.dto.announcement.AnnouncementUpdateRequest;
import com.example.campus.dto.company.CompanyResponse;
import com.example.campus.dto.job.JobResponse;
import com.example.campus.dto.message.MessageCreateRequest;
import com.example.campus.dto.portal.admin.AdminDashboardSummary;
import com.example.campus.dto.portal.admin.AnnouncementPublishRequest;
import com.example.campus.dto.portal.admin.CompanyAuditRequest;
import com.example.campus.dto.portal.admin.JobAuditRequest;
import com.example.campus.dto.portal.admin.UserStatusRequest;
import com.example.campus.dto.user.UserResponse;
import com.example.campus.dto.user.UserUpdateRequest;
import com.example.campus.entity.TsukiAdmin;
import com.example.campus.entity.TsukiCompany;
import com.example.campus.entity.TsukiJob;
import com.example.campus.entity.UserRole;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiAdminRepository;
import com.example.campus.repository.TsukiApplicationRepository;
import com.example.campus.repository.TsukiCompanyRepository;
import com.example.campus.repository.TsukiJobRepository;
import com.example.campus.repository.TsukiMessageRepository;
import com.example.campus.repository.TsukiUserRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminPortalService {

    private static final Set<String> COMPANY_AUDIT_STATUSES = Set.of("pending", "approved", "rejected");
    private static final Set<String> JOB_AUDIT_STATUSES = Set.of("pending", "approved", "rejected", "closed");

    private final TsukiAdminRepository adminRepository;
    private final TsukiUserRepository userRepository;
    private final TsukiCompanyRepository companyRepository;
    private final TsukiJobRepository jobRepository;
    private final TsukiApplicationRepository applicationRepository;
    private final TsukiMessageRepository messageRepository;

    private final CompanyService companyService;
    private final JobService jobService;
    private final AnnouncementService announcementService;
    private final MessageService messageService;
    private final TsukiUserService userService;

    @Transactional(readOnly = true)
    public AdminDashboardSummary loadSummary(Long adminUserId) {
        TsukiAdmin admin = requireAdmin(adminUserId);
        long totalStudents = userRepository.countByRole(UserRole.STUDENT);
        long totalCompanies = userRepository.countByRole(UserRole.COMPANY);
        long pendingCompanies = companyRepository.countByAuditStatus("pending");
        long totalJobs = jobRepository.count();
        long approvedJobs = jobRepository.countByStatus("approved");
        long pendingJobs = jobRepository.countByStatus("pending");
        long totalApplications = applicationRepository.count();
        Map<String, Long> breakdown = new LinkedHashMap<>();
        breakdown.put("待查看", applicationRepository.countByStatus("待查看"));
        breakdown.put("已查看", applicationRepository.countByStatus("已查看"));
        breakdown.put("面试中", applicationRepository.countByStatus("面试中"));
        breakdown.put("录用", applicationRepository.countByStatus("录用"));
        breakdown.put("拒绝", applicationRepository.countByStatus("拒绝"));
        long unreadMessages = messageRepository.countByReceiver_IdAndIsRead(admin.getUser().getId(), Boolean.FALSE);
        return new AdminDashboardSummary(totalStudents, totalCompanies, pendingCompanies, totalJobs, approvedJobs,
                pendingJobs, totalApplications, breakdown, unreadMessages);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> listPendingCompanies() {
        return companyRepository.findByAuditStatus("pending").stream()
                .map(company -> companyService.findById(company.getId()))
                .toList();
    }

    @Transactional
    public CompanyResponse reviewCompany(Long adminUserId, Long companyId, CompanyAuditRequest request) {
        TsukiAdmin admin = requireAdmin(adminUserId);
        TsukiCompany company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到企业信息"));
        String status = normalizeStatus(request.status(), COMPANY_AUDIT_STATUSES);
        company.setAuditStatus(status);
        company.setAuditReason(request.reason());
        companyRepository.save(company);
        if (!"pending".equals(status)) {
            String title = "企业认证审核结果";
            String content = "您的企业认证审核结果为：" + ("approved".equals(status) ? "通过" : "未通过")
                    + (request.reason() != null && !request.reason().isBlank() ? "，备注：" + request.reason() : "");
            messageService.create(new MessageCreateRequest(admin.getUser().getId(), company.getUser().getId(), title,
                    content, null));
        }
        return companyService.findById(company.getId());
    }

    @Transactional(readOnly = true)
    public List<JobResponse> listPendingJobs() {
        return jobRepository.findByStatus("pending").stream()
                .map(job -> jobService.findById(job.getId()))
                .toList();
    }

    @Transactional
    public JobResponse reviewJob(Long adminUserId, Long jobId, JobAuditRequest request) {
        TsukiAdmin admin = requireAdmin(adminUserId);
        TsukiJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到职位信息"));
        String status = normalizeStatus(request.status(), JOB_AUDIT_STATUSES);
        job.setStatus(status);
        jobRepository.save(job);
        String title = "职位审核结果通知";
        String content = "您发布的职位《" + job.getJobTitle() + "》审核结果：" + translateStatus(status)
                + (request.reason() != null && !request.reason().isBlank() ? "，备注：" + request.reason() : "");
        messageService.create(new MessageCreateRequest(admin.getUser().getId(), job.getCompany().getUser().getId(),
                title, content, null));
        return jobService.findById(job.getId());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listUsers() {
        return userService.findAll();
    }

    @Transactional
    public UserResponse updateUserStatus(Long userId, UserStatusRequest request) {
        return userService.update(userId, new UserUpdateRequest(null, null, null, null, null, request.status()));
    }

    @Transactional
    public AnnouncementResponse publishAnnouncement(Long adminUserId, AnnouncementPublishRequest request) {
        TsukiAdmin admin = requireAdmin(adminUserId);
        AnnouncementCreateRequest createRequest = new AnnouncementCreateRequest(admin.getId(), request.title(),
                request.content(), request.target());
        return announcementService.create(createRequest);
    }

    @Transactional
    public AnnouncementResponse updateAnnouncement(Long adminUserId, Long announcementId,
            AnnouncementPublishRequest request) {
        requireAdmin(adminUserId);
        AnnouncementUpdateRequest updateRequest = new AnnouncementUpdateRequest(request.title(), request.content(),
                request.target());
        return announcementService.update(announcementId, updateRequest);
    }

    @Transactional
    public void deleteAnnouncement(Long adminUserId, Long announcementId) {
        requireAdmin(adminUserId);
        announcementService.delete(announcementId);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> listAnnouncementsForTarget(String target) {
        List<String> targets = new ArrayList<>();
        targets.add("all");
        if (target != null && !target.isBlank()) {
            targets.add(target.toLowerCase(Locale.ROOT));
        }
        return announcementService.findByTargets(targets);
    }

    private TsukiAdmin requireAdmin(Long userId) {
        return adminRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("当前账号不是系统管理员"));
    }

    private String normalizeStatus(String raw, Set<String> allowed) {
        if (raw == null || raw.isBlank()) {
            throw new IllegalArgumentException("状态不能为空");
        }
        String normalized = raw.toLowerCase(Locale.ROOT);
        if (!allowed.contains(normalized)) {
            throw new IllegalArgumentException("状态仅支持: " + String.join("/", allowed));
        }
        return normalized;
    }

    private String translateStatus(String status) {
        return switch (status) {
            case "approved" -> "已通过";
            case "rejected" -> "已驳回";
            case "closed" -> "已关闭";
            default -> "待审核";
        };
    }
}
