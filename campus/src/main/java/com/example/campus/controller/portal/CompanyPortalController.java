package com.example.campus.controller.portal;

import com.example.campus.dto.announcement.AnnouncementResponse;
import com.example.campus.dto.company.CompanyResponse;
import com.example.campus.dto.application.ApplicationResponse;
import com.example.campus.dto.discussion.DiscussionCreateRequest;
import com.example.campus.dto.discussion.DiscussionResponse;
import com.example.campus.dto.discussion.DiscussionCommentCreateRequest;
import com.example.campus.dto.discussion.DiscussionCommentResponse;
import com.example.campus.dto.finance.FinancialTransactionResponse;
import com.example.campus.dto.job.JobResponse;
import com.example.campus.dto.portal.company.ApplicationStatusRequest;
import com.example.campus.dto.portal.company.CompanyJobRequest;
import com.example.campus.dto.portal.company.CompanyProfileRequest;
import com.example.campus.dto.portal.company.CompanyInviteVerifyRequest;
import com.example.campus.dto.portal.company.CompanySubscriptionRequest;
import com.example.campus.dto.portal.company.CompanyTransactionRequest;
import com.example.campus.dto.portal.company.JobStatusUpdateRequest;
import com.example.campus.dto.wallet.WalletSummaryResponse;
import com.example.campus.security.UserPrincipal;
import com.example.campus.service.CompanyPortalService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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

    @PostMapping(value = "/profile/license", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CompanyResponse uploadLicense(@AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("file") MultipartFile file) {
        return companyPortalService.uploadLicense(principal.getUserId(), file);
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

    @GetMapping("/transactions")
    public List<FinancialTransactionResponse> listTransactions(@AuthenticationPrincipal UserPrincipal principal) {
        return companyPortalService.listTransactions(principal.getUserId());
    }

    @PostMapping("/transactions")
    public FinancialTransactionResponse submitTransaction(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CompanyTransactionRequest request) {
        return companyPortalService.submitTransaction(principal.getUserId(), request);
    }

    @GetMapping("/wallet")
    public WalletSummaryResponse loadWallet(@AuthenticationPrincipal UserPrincipal principal) {
        return companyPortalService.loadWallet(principal.getUserId());
    }

    @PostMapping("/wallet/subscription")
    public FinancialTransactionResponse purchaseSubscription(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CompanySubscriptionRequest request) {
        return companyPortalService.purchaseSubscription(principal.getUserId(), request);
    }

    @GetMapping("/discussions")
    public List<DiscussionResponse> listDiscussions(@AuthenticationPrincipal UserPrincipal principal) {
        return companyPortalService.listDiscussions(principal.getUserId());
    }

    @PostMapping("/discussions")
    public DiscussionResponse createDiscussion(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DiscussionCreateRequest request) {
        // 公司端创建时，忽略外部传入的 companyId，固定发到本企业
        DiscussionCreateRequest effective = new DiscussionCreateRequest(null, request.title(), request.content());
        return companyPortalService.createDiscussion(principal.getUserId(), effective);
    }

    @PostMapping("/discussions/{postId}/comments")
    public DiscussionCommentResponse createDiscussionComment(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId, @Valid @RequestBody DiscussionCommentCreateRequest request) {
        DiscussionCommentCreateRequest effective =
                new DiscussionCommentCreateRequest(postId, request.parentCommentId(), request.content());
        return companyPortalService.createDiscussionComment(principal.getUserId(), effective);
    }

    @GetMapping("/messages/unread-count")
    public long countUnreadMessages(@AuthenticationPrincipal UserPrincipal principal) {
        return companyPortalService.countUnreadMessages(principal.getUserId());
    }

    @GetMapping("/announcements")
    public List<AnnouncementResponse> listAnnouncements() {
        return companyPortalService.listAnnouncements();
    }

    @PostMapping("/invite/activate")
    public CompanyResponse activateInvite(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CompanyInviteVerifyRequest request) {
        return companyPortalService.activateInvite(principal.getUserId(), request);
    }
}
