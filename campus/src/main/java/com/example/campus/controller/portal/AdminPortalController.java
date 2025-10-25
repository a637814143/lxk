package com.example.campus.controller.portal;

import com.example.campus.dto.announcement.AnnouncementResponse;
import com.example.campus.dto.backup.BackupCreateRequest;
import com.example.campus.dto.backup.BackupResponse;
import com.example.campus.dto.company.CompanyResponse;
import com.example.campus.dto.discussion.DiscussionResponse;
import com.example.campus.dto.discussion.DiscussionReviewRequest;
import com.example.campus.dto.finance.FinancialTransactionRequest;
import com.example.campus.dto.finance.FinancialTransactionResponse;
import com.example.campus.dto.finance.FinancialTransactionStatusRequest;
import com.example.campus.dto.job.JobResponse;
import com.example.campus.dto.portal.admin.AdminDashboardSummary;
import com.example.campus.dto.portal.admin.AnnouncementPublishRequest;
import com.example.campus.dto.portal.admin.CompanyAuditRequest;
import com.example.campus.dto.portal.admin.CompanyInviteCreateRequest;
import com.example.campus.dto.portal.admin.CompanyInviteResponse;
import com.example.campus.dto.portal.admin.JobAuditRequest;
import com.example.campus.dto.portal.admin.UserStatusRequest;
import com.example.campus.dto.user.UserResponse;
import com.example.campus.security.UserPrincipal;
import com.example.campus.service.AdminPortalService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portal/admin")
@RequiredArgsConstructor
public class AdminPortalController {

    private final AdminPortalService adminPortalService;

    @GetMapping("/summary")
    public AdminDashboardSummary loadSummary(@AuthenticationPrincipal UserPrincipal principal) {
        return adminPortalService.loadSummary(principal.getUserId());
    }

    @GetMapping("/companies/pending")
    public List<CompanyResponse> listPendingCompanies() {
        return adminPortalService.listPendingCompanies();
    }

    @PatchMapping("/companies/{companyId}/review")
    public CompanyResponse reviewCompany(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long companyId,
            @Valid @RequestBody CompanyAuditRequest request) {
        return adminPortalService.reviewCompany(principal.getUserId(), companyId, request);
    }

    @GetMapping("/invites")
    public List<CompanyInviteResponse> listInvites() {
        return adminPortalService.listInvites();
    }

    @PostMapping("/invites")
    public CompanyInviteResponse createInvite(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CompanyInviteCreateRequest request) {
        return adminPortalService.createInvite(principal.getUserId(), request);
    }

    @PostMapping("/invites/{inviteId}/revoke")
    public CompanyInviteResponse revokeInvite(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long inviteId) {
        return adminPortalService.revokeInvite(principal.getUserId(), inviteId);
    }

    @GetMapping("/jobs/pending")
    public List<JobResponse> listPendingJobs() {
        return adminPortalService.listPendingJobs();
    }

    @GetMapping("/discussions/pending")
    public List<DiscussionResponse> listPendingDiscussions() {
        return adminPortalService.listPendingDiscussions();
    }

    @PostMapping("/discussions/{discussionId}/review")
    public DiscussionResponse reviewDiscussion(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long discussionId, @Valid @RequestBody DiscussionReviewRequest request) {
        return adminPortalService.reviewDiscussion(principal.getUserId(), discussionId, request);
    }

    @PatchMapping("/jobs/{jobId}/review")
    public JobResponse reviewJob(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long jobId,
            @Valid @RequestBody JobAuditRequest request) {
        return adminPortalService.reviewJob(principal.getUserId(), jobId, request);
    }

    @GetMapping("/transactions")
    public List<FinancialTransactionResponse> listTransactions() {
        return adminPortalService.listTransactions();
    }

    @PostMapping("/transactions")
    public FinancialTransactionResponse createTransaction(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody FinancialTransactionRequest request) {
        return adminPortalService.createTransaction(principal.getUserId(), request);
    }

    @PatchMapping("/transactions/{transactionId}")
    public FinancialTransactionResponse updateTransaction(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long transactionId, @Valid @RequestBody FinancialTransactionStatusRequest request) {
        return adminPortalService.updateTransactionStatus(principal.getUserId(), transactionId, request);
    }

    @GetMapping("/users")
    public List<UserResponse> listUsers() {
        return adminPortalService.listUsers();
    }

    @PatchMapping("/users/{userId}/status")
    public UserResponse updateUserStatus(@PathVariable Long userId, @Valid @RequestBody UserStatusRequest request) {
        return adminPortalService.updateUserStatus(userId, request);
    }

    @GetMapping("/announcements")
    public List<AnnouncementResponse> listAnnouncements(@RequestParam(required = false) String target) {
        return adminPortalService.listAnnouncementsForTarget(target);
    }

    @PostMapping("/announcements")
    public AnnouncementResponse publishAnnouncement(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AnnouncementPublishRequest request) {
        return adminPortalService.publishAnnouncement(principal.getUserId(), request);
    }

    @PutMapping("/announcements/{announcementId}")
    public AnnouncementResponse updateAnnouncement(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long announcementId, @Valid @RequestBody AnnouncementPublishRequest request) {
        return adminPortalService.updateAnnouncement(principal.getUserId(), announcementId, request);
    }

    @DeleteMapping("/announcements/{announcementId}")
    public void deleteAnnouncement(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long announcementId) {
        adminPortalService.deleteAnnouncement(principal.getUserId(), announcementId);
    }

    @GetMapping("/backups")
    public List<BackupResponse> listBackups() {
        return adminPortalService.listBackups();
    }

    @PostMapping("/backups")
    public BackupResponse createBackup(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody BackupCreateRequest request) {
        return adminPortalService.createBackup(principal.getUserId(), request);
    }
}
