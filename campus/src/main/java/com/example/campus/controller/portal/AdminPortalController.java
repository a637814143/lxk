package com.example.campus.controller.portal;

import com.example.campus.dto.announcement.AnnouncementResponse;
import com.example.campus.dto.company.CompanyResponse;
import com.example.campus.dto.job.JobResponse;
import com.example.campus.dto.portal.admin.AdminDashboardSummary;
import com.example.campus.dto.portal.admin.AnnouncementPublishRequest;
import com.example.campus.dto.portal.admin.CompanyAuditRequest;
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

    @GetMapping("/jobs/pending")
    public List<JobResponse> listPendingJobs() {
        return adminPortalService.listPendingJobs();
    }

    @PatchMapping("/jobs/{jobId}/review")
    public JobResponse reviewJob(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long jobId,
            @Valid @RequestBody JobAuditRequest request) {
        return adminPortalService.reviewJob(principal.getUserId(), jobId, request);
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
}
