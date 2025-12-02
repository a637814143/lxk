package com.example.campus.service;

import com.example.campus.dto.announcement.AnnouncementResponse;
import com.example.campus.dto.announcement.AnnouncementCreateRequest;
import com.example.campus.dto.announcement.AnnouncementUpdateRequest;
import com.example.campus.dto.company.CompanyInviteCreateRequest;
import com.example.campus.dto.company.CompanyInviteResponse;
import com.example.campus.dto.company.CompanyInviteStatusRequest;
import com.example.campus.dto.company.CompanyResponse;
import com.example.campus.dto.discussion.DiscussionResponse;
import com.example.campus.dto.discussion.DiscussionReviewRequest;
import com.example.campus.dto.finance.FinancialTransactionRequest;
import com.example.campus.dto.finance.FinancialTransactionResponse;
import com.example.campus.dto.finance.FinancialTransactionStatusRequest;
import com.example.campus.dto.message.MessageCreateRequest;
import com.example.campus.dto.portal.admin.AdminDashboardSummary;
import com.example.campus.dto.portal.admin.AnnouncementPublishRequest;
import com.example.campus.dto.portal.admin.CompanyAuditRequest;
import com.example.campus.dto.portal.admin.UserStatusRequest;
import com.example.campus.dto.user.UserResponse;
import com.example.campus.dto.user.UserUpdateRequest;
import com.example.campus.dto.wallet.WalletSummaryResponse;
import com.example.campus.entity.TsukiAdmin;
import com.example.campus.entity.TsukiCompany;
import com.example.campus.entity.TsukiWallet;
import com.example.campus.entity.UserRole;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiAdminRepository;
import com.example.campus.repository.TsukiApplicationRepository;
import com.example.campus.repository.TsukiCompanyRepository;
import com.example.campus.repository.TsukiJobRepository;
import com.example.campus.repository.TsukiDiscussionPostRepository;
import com.example.campus.repository.TsukiDiscussionCommentRepository;
import com.example.campus.repository.TsukiMessageRepository;
import com.example.campus.repository.TsukiUserRepository;
import com.example.campus.repository.TsukiWalletRepository;
import com.example.campus.dto.backup.BackupResponse;
import com.example.campus.dto.backup.BackupCreateRequest;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.LinkedHashMap;
import java.util.ArrayList;
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
    // Job audit statuses constant removed — job review workflow is disabled

    private final TsukiAdminRepository adminRepository;
    private final TsukiUserRepository userRepository;
    private final TsukiCompanyRepository companyRepository;
    private final TsukiJobRepository jobRepository;
    private final TsukiApplicationRepository applicationRepository;
    private final TsukiMessageRepository messageRepository;
    private final TsukiDiscussionPostRepository discussionPostRepository;
    private final TsukiDiscussionCommentRepository discussionCommentRepository;

    private final CompanyService companyService;
    private final JobService jobService;
    private final AnnouncementService announcementService;
    private final MessageService messageService;
    private final TsukiUserService userService;
    private final FinancialTransactionService financialTransactionService;
    private final DataBackupService dataBackupService;
    private final DiscussionService discussionService;
    private final CompanyInviteService companyInviteService;
    private final TsukiWalletRepository walletRepository;
    private final EmailService emailService;

    @Transactional(readOnly = true)
    public AdminDashboardSummary loadSummary(Long adminUserId) {
        TsukiAdmin admin = requireAdmin(adminUserId);
        long totalStudents = userRepository.countByRole(UserRole.STUDENT);
        long totalCompanies = userRepository.countByRole(UserRole.COMPANY);
        long pendingCompanies = companyRepository.countByAuditStatus("pending");
        long totalJobs = jobRepository.count();
        long approvedJobs = jobRepository.countByStatus("approved");
        long totalApplications = applicationRepository.count();
        Map<String, Long> breakdown = new LinkedHashMap<>();
        breakdown.put("待查看", applicationRepository.countByStatus("待查看"));
        breakdown.put("已查看", applicationRepository.countByStatus("已查看"));
        breakdown.put("面试中", applicationRepository.countByStatus("面试中"));
        breakdown.put("录用", applicationRepository.countByStatus("录用"));
        breakdown.put("拒绝", applicationRepository.countByStatus("拒绝"));
        long unreadMessages = messageRepository.countByReceiver_IdAndIsRead(admin.getUser().getId(), Boolean.FALSE);
        long pendingDiscussions = discussionPostRepository.countByStatus("pending");
        long pendingComments = discussionCommentRepository.countByStatus("pending");
        return new AdminDashboardSummary(totalStudents, totalCompanies, pendingCompanies, totalJobs, approvedJobs,
                totalApplications, breakdown, unreadMessages, pendingDiscussions, pendingComments);
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> listPendingCompanies() {
        return companyRepository.findByAuditStatus("pending").stream()
                .map(company -> companyService.findById(company.getId()))
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyDetail(Long adminUserId, Long companyId) {
        requireAdmin(adminUserId);
        return companyService.findById(companyId);
    }

    @Transactional(readOnly = true)
    public List<FinancialTransactionResponse> listTransactions() {
        return financialTransactionService.findAll();
    }

    @Transactional
    public FinancialTransactionResponse createTransaction(Long adminUserId, FinancialTransactionRequest request) {
        return financialTransactionService.createByAdmin(adminUserId, request);
    }

    @Transactional
    public FinancialTransactionResponse updateTransactionStatus(Long adminUserId, Long transactionId,
            FinancialTransactionStatusRequest request) {
        return financialTransactionService.updateStatus(adminUserId, transactionId, request);
    }

    @Transactional
    public WalletSummaryResponse loadWallet(Long adminUserId) {
        TsukiAdmin admin = requireAdmin(adminUserId);
        TsukiWallet wallet = walletRepository.findByOwnerIdAndOwnerType(admin.getId(), "admin")
                .orElseGet(() -> walletRepository.save(TsukiWallet.builder()
                        .ownerId(admin.getId())
                        .ownerType("admin")
                        .balance(BigDecimal.ZERO)
                        .build()));
        return new WalletSummaryResponse(wallet.getOwnerId(), wallet.getOwnerType(), wallet.getBalance(),
                wallet.getUpdatedAt(), null, null, null, 0L);
    }

    @Transactional(readOnly = true)
    public List<CompanyInviteResponse> listCompanyInvites() {
        return companyInviteService.listAll();
    }

    @Transactional
    public CompanyInviteResponse createCompanyInvite(Long adminUserId, CompanyInviteCreateRequest request) {
        return companyInviteService.create(adminUserId, request);
    }

    @Transactional
    public CompanyInviteResponse updateCompanyInvite(Long adminUserId, Long inviteId,
            CompanyInviteStatusRequest request) {
        return companyInviteService.updateStatus(adminUserId, inviteId, request);
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
        CompanyInviteResponse invite = null;
        if ("approved".equals(status) && !companyInviteService.isCompanyActivated(company.getCompanyName())) {
            invite = companyInviteService.create(adminUserId,
                    new CompanyInviteCreateRequest("自动生成：" + Objects.toString(company.getCompanyName(), ""),
                            company.getCompanyName()));
        }
        if (!"pending".equals(status)) {
            String title = "企业认证审核结果";
            String content = "您的企业认证审核结果为：" + ("approved".equals(status) ? "通过" : "未通过")
                    + (request.reason() != null && !request.reason().isBlank() ? "，备注：" + request.reason() : "");
            if (invite != null) {
                content += "\n邀请码：" + invite.code();
            }
            messageService.create(new MessageCreateRequest(admin.getUser().getId(), company.getUser().getId(), title,
                    content, null));
            if (invite != null && company.getUser() != null
                    && org.springframework.util.StringUtils.hasText(company.getUser().getEmail())) {
                String subject = "企业审核通过，邀请码已生成";
                String body = "您的企业（" + company.getCompanyName() + "）已通过审核。\n"
                        + "邀请码：" + invite.code() + "\n"
                        + "请使用邀请码在企业中心完成激活。";
                emailService.sendPlainText(company.getUser().getEmail(), subject, body);
            }
        }
        return companyService.findById(company.getId());
    }

    // listPendingJobs removed

    @Transactional(readOnly = true)
    public List<DiscussionResponse> listPendingDiscussions() {
        return discussionService.findPending();
    }

    @Transactional(readOnly = true)
    public List<DiscussionResponse> listDiscussions(String status) {
        return discussionService.findByStatusForAdmin(status);
    }

    @Transactional
    public DiscussionResponse reviewDiscussion(Long adminUserId, Long discussionId, DiscussionReviewRequest request) {
        return discussionService.review(adminUserId, discussionId, request);
    }

    @Transactional
    public void deleteDiscussion(Long adminUserId, Long discussionId) {
        discussionService.adminDeletePost(adminUserId, discussionId);
    }

    @Transactional(readOnly = true)
    public java.util.List<com.example.campus.dto.discussion.DiscussionCommentResponse> listPendingDiscussionComments() {
        return discussionService.findPendingComments();
    }

    @Transactional(readOnly = true)
    public java.util.List<com.example.campus.dto.discussion.DiscussionCommentResponse> listDiscussionComments(
            String status) {
        return discussionService.findCommentsByStatus(status);
    }

    @Transactional
    public com.example.campus.dto.discussion.DiscussionCommentResponse reviewDiscussionComment(Long adminUserId,
            Long commentId, DiscussionReviewRequest request) {
        return discussionService.reviewComment(adminUserId, commentId, request);
    }

    @Transactional
    public void deleteDiscussionComment(Long adminUserId, Long commentId) {
        discussionService.adminDeleteComment(adminUserId, commentId);
    }

    // reviewJob removed

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

    @Transactional(readOnly = true)
    public List<BackupResponse> listBackups() {
        return dataBackupService.findAll();
    }

    @Transactional
    public BackupResponse createBackup(Long adminUserId, BackupCreateRequest request) {
        return dataBackupService.createBackup(adminUserId, request);
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
