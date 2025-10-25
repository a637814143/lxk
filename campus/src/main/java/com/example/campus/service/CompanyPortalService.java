package com.example.campus.service;

import com.example.campus.dto.announcement.AnnouncementResponse;
import com.example.campus.dto.application.ApplicationUpdateRequest;
import com.example.campus.dto.application.ApplicationResponse;
import com.example.campus.dto.company.CompanyResponse;
import com.example.campus.dto.company.CompanyUpdateRequest;
import com.example.campus.dto.discussion.DiscussionCreateRequest;
import com.example.campus.dto.discussion.DiscussionResponse;
import com.example.campus.dto.finance.FinancialTransactionResponse;
import com.example.campus.dto.job.JobCreateRequest;
import com.example.campus.dto.job.JobResponse;
import com.example.campus.dto.job.JobUpdateRequest;
import com.example.campus.dto.message.MessageCreateRequest;
import com.example.campus.dto.portal.company.ApplicationStatusRequest;
import com.example.campus.dto.portal.company.CompanyJobRequest;
import com.example.campus.dto.portal.company.CompanyProfileRequest;
import com.example.campus.dto.portal.company.JobStatusUpdateRequest;
import com.example.campus.dto.company.CompanyCreateRequest;
import com.example.campus.dto.portal.company.CompanyTransactionRequest;
import com.example.campus.dto.resume.ResumeAttachmentResponse;
import com.example.campus.entity.TsukiApplication;
import com.example.campus.entity.TsukiCompany;
import com.example.campus.entity.TsukiJob;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiApplicationRepository;
import com.example.campus.repository.TsukiCompanyRepository;
import com.example.campus.repository.TsukiJobRepository;
import com.example.campus.repository.TsukiMessageRepository;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CompanyPortalService {

    private final CompanyService companyService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final MessageService messageService;
    private final AnnouncementService announcementService;
    private final FinancialTransactionService financialTransactionService;
    private final DiscussionService discussionService;
    private final FileStorageService fileStorageService;

    private final TsukiCompanyRepository companyRepository;
    private final TsukiJobRepository jobRepository;
    private final TsukiApplicationRepository applicationRepository;
    private final TsukiMessageRepository messageRepository;

    @Transactional(readOnly = true)
    public CompanyResponse loadProfile(Long userId) {
        return companyService.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("请先完善企业资料"));
    }

    @Transactional
    public CompanyResponse saveProfile(Long userId, CompanyProfileRequest request) {
        return companyRepository.findByUser_Id(userId)
                .map(existing -> companyService.update(existing.getId(),
                        new CompanyUpdateRequest(request.companyName(), request.licenseNumber(), request.industry(),
                                request.address(), request.website(), request.description(), request.logo(),
                                request.licenseDocument(), null, null)))
                .orElseGet(() -> companyService.create(new CompanyCreateRequest(userId, request.companyName(),
                        request.licenseNumber(), request.industry(), request.address(), request.website(),
                        request.description(), request.logo(), request.licenseDocument(), "approved", null)));
    }

    @Transactional(readOnly = true)
    public List<JobResponse> listJobs(Long userId) {
        TsukiCompany company = requireCompany(userId);
        return jobService.findByCompanyId(company.getId());
    }

    @Transactional
    public JobResponse createJob(Long userId, CompanyJobRequest request) {
        TsukiCompany company = requireCompany(userId);
        JobCreateRequest createRequest = new JobCreateRequest(company.getId(), request.jobTitle(), request.jobType(),
                request.salaryRange(), request.location(), request.requirement(), request.description(), "approved");
        return jobService.create(createRequest);
    }

    @Transactional
    public JobResponse updateJob(Long userId, Long jobId, CompanyJobRequest request) {
        TsukiCompany company = requireCompany(userId);
        TsukiJob job = requireJob(jobId, company.getId());
        JobUpdateRequest updateRequest = new JobUpdateRequest(request.jobTitle(), request.jobType(),
                request.salaryRange(), request.location(), request.requirement(), request.description(), null);
        return jobService.update(job.getId(), updateRequest);
    }

    @Transactional
    public JobResponse updateJobStatus(Long userId, Long jobId, JobStatusUpdateRequest request) {
        TsukiCompany company = requireCompany(userId);
        TsukiJob job = requireJob(jobId, company.getId());
        JobUpdateRequest updateRequest = new JobUpdateRequest(null, null, null, null, null, null, request.status());
        return jobService.update(job.getId(), updateRequest);
    }

    @Transactional
    public CompanyResponse uploadLicense(Long userId, MultipartFile file) {
        TsukiCompany company = requireCompany(userId);
        try {
            if (company.getLicenseDocument() != null) {
                fileStorageService.deleteIfExists(company.getLicenseDocument());
            }
            String path = fileStorageService.store(file, FileStorageService.StorageArea.LICENSE,
                    "license-" + company.getId());
            company.setLicenseDocument(path);
            companyRepository.save(company);
            return companyService.findById(company.getId());
        } catch (Exception ex) {
            throw new IllegalStateException("上传证照失败: " + ex.getMessage(), ex);
        }
    }

    @Transactional(readOnly = true)
    public List<FinancialTransactionResponse> listTransactions(Long userId) {
        TsukiCompany company = requireCompany(userId);
        return financialTransactionService.findByCompany(company.getId());
    }

    @Transactional
    public FinancialTransactionResponse submitTransaction(Long userId, CompanyTransactionRequest request) {
        return financialTransactionService.submitByCompany(userId, request);
    }

    @Transactional(readOnly = true)
    public List<DiscussionResponse> listDiscussions(Long userId) {
        return discussionService.findByCompany(userId);
    }

    @Transactional
    public DiscussionResponse createDiscussion(Long userId, DiscussionCreateRequest request) {
        return discussionService.create(userId, request);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listApplications(Long userId) {
        TsukiCompany company = requireCompany(userId);
        return applicationService.findByCompanyId(company.getId());
    }

    @Transactional
    public ApplicationResponse viewApplication(Long userId, Long applicationId) {
        TsukiCompany company = requireCompany(userId);
        TsukiApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到投递记录"));
        if (!application.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException("无法查看其他企业的投递记录");
        }
        if ("待查看".equals(application.getStatus())) {
            return applicationService.update(applicationId,
                    new ApplicationUpdateRequest("已查看", application.getDecisionNote()));
        }
        return applicationService.findById(applicationId);
    }

    @Transactional
    public ApplicationResponse updateApplicationStatus(Long userId, Long applicationId, ApplicationStatusRequest request) {
        TsukiCompany company = requireCompany(userId);
        TsukiApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到投递记录"));
        if (!application.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException("无法操作其他企业的投递记录");
        }
        String normalizedStatus = request.status();
        String decisionNote = request.decisionNote();
        if (decisionNote != null) {
            decisionNote = decisionNote.trim();
        }
        if ("拒绝".equals(normalizedStatus) && (decisionNote == null || decisionNote.isBlank())) {
            throw new IllegalArgumentException("请填写拒绝原因");
        }
        ApplicationResponse response = applicationService
                .update(applicationId, new ApplicationUpdateRequest(normalizedStatus, decisionNote));
        boolean notifyStudent = request.notifyStudent() == null || Boolean.TRUE.equals(request.notifyStudent());
        if (notifyStudent) {
            String title;
            String content;
            if ("面试中".equals(normalizedStatus)) {
                title = "面试邀请通知";
                content = decisionNote != null && !decisionNote.isBlank()
                        ? decisionNote
                        : "您好，我们已查看您的简历，邀请您进入面试环节，请留意后续安排。";
            } else if ("拒绝".equals(normalizedStatus)) {
                title = "简历评估结果";
                content = decisionNote;
            } else if ("录用".equals(normalizedStatus)) {
                title = "录用通知";
                content = decisionNote != null && !decisionNote.isBlank()
                        ? decisionNote
                        : "恭喜您顺利通过，后续将有专人与您联系。";
            } else {
                title = "投递状态更新";
                content = decisionNote != null && !decisionNote.isBlank()
                        ? decisionNote
                        : "您好，您的投递状态已更新为：" + normalizedStatus + "。";
            }
            messageService.create(new MessageCreateRequest(company.getUser().getId(),
                    application.getStudent().getUser().getId(), title, content, null));
        }
        return response;
    }

    @Transactional(readOnly = true)
    public List<ResumeAttachmentResponse> listApplicationAttachments(Long userId, Long applicationId) {
        TsukiCompany company = requireCompany(userId);
        TsukiApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到投递记录"));
        if (!application.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException("无法查看其他企业的投递附件");
        }
        ApplicationResponse response = applicationService.findById(applicationId);
        if (response.resume() == null || response.resume().attachments() == null) {
            return List.of();
        }
        return response.resume().attachments();
    }

    @Transactional(readOnly = true)
    public long countUnreadMessages(Long userId) {
        return messageRepository.countByReceiver_IdAndIsRead(userId, Boolean.FALSE);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> listAnnouncements() {
        return announcementService.findByTargets(List.of("all", "company"));
    }

    private TsukiCompany requireCompany(Long userId) {
        return companyRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("请先完善企业资料"));
    }

    private TsukiJob requireJob(Long jobId, Long companyId) {
        TsukiJob job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到职位信息"));
        if (!job.getCompany().getId().equals(companyId)) {
            throw new IllegalArgumentException("无法操作其他企业的职位");
        }
        return job;
    }
}
