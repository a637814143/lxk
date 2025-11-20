package com.example.campus.controller.portal;

import com.example.campus.dto.announcement.AnnouncementResponse;
import com.example.campus.dto.application.ApplicationResponse;
import com.example.campus.dto.job.JobResponse;
import com.example.campus.dto.message.MessageResponse;
import com.example.campus.dto.portal.student.StudentApplicationRequest;
import com.example.campus.dto.portal.student.StudentProfileRequest;
import com.example.campus.dto.portal.student.StudentResumeRequest;
import com.example.campus.dto.discussion.DiscussionCreateRequest;
import com.example.campus.dto.discussion.DiscussionResponse;
import com.example.campus.dto.discussion.DiscussionCommentCreateRequest;
import com.example.campus.dto.discussion.DiscussionCommentResponse;
import com.example.campus.dto.resume.ResumeResponse;
import com.example.campus.dto.resume.ResumeUpdateRequest;
import com.example.campus.dto.student.StudentResponse;
import com.example.campus.security.UserPrincipal;
import com.example.campus.service.StudentPortalService;
import jakarta.validation.Valid;
import java.util.Map;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/portal/student")
@RequiredArgsConstructor
public class StudentPortalController {

    private final StudentPortalService studentPortalService;

    @GetMapping("/profile")
    public StudentResponse loadProfile(@AuthenticationPrincipal UserPrincipal principal) {
        return studentPortalService.loadProfile(principal.getUserId());
    }

    @PutMapping("/profile")
    public StudentResponse saveProfile(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StudentProfileRequest request) {
        return studentPortalService.saveProfile(principal.getUserId(), request);
    }

    @GetMapping("/resumes")
    public List<ResumeResponse> listResumes(@AuthenticationPrincipal UserPrincipal principal) {
        return studentPortalService.listResumes(principal.getUserId());
    }

    @PostMapping("/resumes")
    public ResumeResponse createResume(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StudentResumeRequest request) {
        return studentPortalService.createResume(principal.getUserId(), request);
    }

    @PostMapping(value = "/resumes/attachments", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, String> uploadResumeAttachment(@AuthenticationPrincipal UserPrincipal principal,
            @RequestPart("file") MultipartFile file) {
        String path = studentPortalService.uploadAttachment(principal.getUserId(), file);
        return Map.of("attachment", path);
    }

    @PostMapping(value = "/resumes/{resumeId}/attachment", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResumeResponse updateResumeAttachment(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long resumeId, @RequestPart("file") MultipartFile file) {
        return studentPortalService.replaceAttachment(principal.getUserId(), resumeId, file);
    }

    @PutMapping("/resumes/{resumeId}")
    public ResumeResponse updateResume(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long resumeId,
            @Valid @RequestBody ResumeUpdateRequest request) {
        return studentPortalService.updateResume(principal.getUserId(), resumeId, request);
    }

    @DeleteMapping("/resumes/{resumeId}")
    public void deleteResume(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long resumeId) {
        studentPortalService.deleteResume(principal.getUserId(), resumeId);
    }

    @GetMapping("/jobs")
    public List<JobResponse> searchJobs(@RequestParam(required = false) String keyword,
            @RequestParam(name = "company", required = false) String companyName,
            @RequestParam(required = false) String jobType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String salaryRange) {
        return studentPortalService.searchJobs(keyword, companyName, jobType, location, salaryRange);
    }

    @PostMapping("/applications")
    public ApplicationResponse apply(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody StudentApplicationRequest request) {
        return studentPortalService.apply(principal.getUserId(), request);
    }

    @GetMapping("/applications")
    public List<ApplicationResponse> listApplications(@AuthenticationPrincipal UserPrincipal principal) {
        return studentPortalService.listApplications(principal.getUserId());
    }

    @GetMapping("/messages")
    public List<MessageResponse> listMessages(@AuthenticationPrincipal UserPrincipal principal) {
        return studentPortalService.listMessages(principal.getUserId());
    }

    @PostMapping("/messages/{messageId}/read")
    public MessageResponse markMessageRead(@AuthenticationPrincipal UserPrincipal principal, @PathVariable Long messageId) {
        return studentPortalService.markMessageRead(principal.getUserId(), messageId);
    }

    @GetMapping("/messages/unread-count")
    public long countUnreadMessages(@AuthenticationPrincipal UserPrincipal principal) {
        return studentPortalService.countUnreadMessages(principal.getUserId());
    }

    @GetMapping("/announcements")
    public List<AnnouncementResponse> listAnnouncements() {
        return studentPortalService.listAnnouncements();
    }

    @PostMapping("/discussions")
    public DiscussionResponse createDiscussion(@AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DiscussionCreateRequest request) {
        return studentPortalService.createDiscussion(principal.getUserId(), request);
    }

    @PostMapping("/discussions/{postId}/comments")
    public DiscussionCommentResponse createComment(@AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long postId, @Valid @RequestBody DiscussionCommentCreateRequest request) {
        // 以路径参数为主，保留前端传入的父评论 ID
        DiscussionCommentCreateRequest effective =
                new DiscussionCommentCreateRequest(postId, request.parentCommentId(), request.content());
        return studentPortalService.createComment(principal.getUserId(), effective);
    }
}

