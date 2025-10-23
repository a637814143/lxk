package com.example.campus.controller.portal;

import com.example.campus.dto.announcement.AnnouncementResponse;
import com.example.campus.dto.application.ApplicationResponse;
import com.example.campus.dto.company.CompanyResponse;
import com.example.campus.dto.job.JobResponse;
import com.example.campus.dto.portal.company.ApplicationStatusRequest;
import com.example.campus.dto.portal.company.CompanyJobRequest;
import com.example.campus.dto.portal.company.CompanyProfileRequest;
import com.example.campus.dto.portal.company.JobStatusUpdateRequest;
import com.example.campus.security.UserPrincipal;
import com.example.campus.service.CompanyPortalService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/portal/company")
@RequiredArgsConstructor
public class CompanyPortalController {

    private final CompanyPortalService companyPortalService;

    @GetMapping("/profile")
    public CompanyResponse loadProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return companyPortalService.loadProfile(principal.getUserId());
    }

    @PutMapping("/profile")
    public CompanyResponse saveProfile(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CompanyProfileRequest request) {
        return companyPortalService.saveProfile(principal.getUserId(), request);
    }

    @GetMapping("/jobs")
    public List<JobResponse> listJobs(@AuthenticationPrincipal UserPrincipal principal) {
        return companyPortalService.listJobs(principal.getUserId());
    }

    @PostMapping("/jobs")
    public JobResponse createJob(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CompanyJobRequest request) {
        return companyPortalService.createJob(principal.getUserId(), request);
    }

    @PutMapping("/jobs/{jobId}")
    public JobResponse updateJob(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long jobId,
            @Valid @RequestBody CompanyJobRequest request) {
        return companyPortalService.updateJob(principal.getUserId(), jobId, request);
    }

    @PatchMapping("/jobs/{jobId}/status")
    public JobResponse updateJobStatus(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long jobId,
            @Valid @RequestBody JobStatusUpdateRequest request) {
        return companyPortalService.updateJobStatus(principal.getUserId(), jobId, request);
    }

    @GetMapping("/applications")
    public List<ApplicationResponse> listApplications(@AuthenticationPrincipal UserPrincipal principal) {
        return companyPortalService.listApplications(principal.getUserId());
    }

    @PatchMapping("/applications/{applicationId}")
    public ApplicationResponse updateApplicationStatus(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long applicationId, @Valid @RequestBody ApplicationStatusRequest request) {
        return companyPortalService.updateApplicationStatus(principal.getUserId(), applicationId, request);
    }

    @GetMapping("/messages/unread-count")
    public long countUnreadMessages(@AuthenticationPrincipal UserPrincipal principal) {
        return companyPortalService.countUnreadMessages(principal.getUserId());
    }

    @GetMapping("/announcements")
    public List<AnnouncementResponse> listAnnouncements() {
        return companyPortalService.listAnnouncements();
    }
}
