package com.example.campus.service;

import com.example.campus.dto.announcement.AnnouncementResponse;
import com.example.campus.dto.application.ApplicationResponse;
import com.example.campus.dto.message.MessageResponse;
import com.example.campus.dto.message.MessageUpdateRequest;
import com.example.campus.dto.discussion.DiscussionCreateRequest;
import com.example.campus.dto.discussion.DiscussionResponse;
import com.example.campus.dto.portal.student.StudentApplicationRequest;
import com.example.campus.dto.portal.student.StudentProfileRequest;
import com.example.campus.dto.portal.student.StudentResumeRequest;
import com.example.campus.dto.resume.ResumeResponse;
import com.example.campus.dto.resume.ResumeUpdateRequest;
import com.example.campus.dto.student.StudentCreateRequest;
import com.example.campus.dto.student.StudentResponse;
import com.example.campus.dto.student.StudentUpdateRequest;
import com.example.campus.entity.TsukiApplication;
import com.example.campus.entity.TsukiJob;
import com.example.campus.entity.TsukiResume;
import com.example.campus.entity.TsukiStudent;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiApplicationRepository;
import com.example.campus.repository.TsukiJobRepository;
import com.example.campus.repository.TsukiMessageRepository;
import com.example.campus.repository.TsukiResumeRepository;
import com.example.campus.repository.TsukiStudentRepository;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentPortalService {

    private final StudentService studentService;
    private final ResumeService resumeService;
    private final ApplicationService applicationService;
    private final MessageService messageService;
    private final JobService jobService;
    private final AnnouncementService announcementService;
    private final DiscussionService discussionService;

    private final TsukiStudentRepository studentRepository;
    private final TsukiResumeRepository resumeRepository;
    private final TsukiApplicationRepository applicationRepository;
    private final TsukiJobRepository jobRepository;
    private final TsukiMessageRepository messageRepository;
    private final FileStorageService fileStorageService;
    private final ResumeAttachmentService resumeAttachmentService;

    @Transactional(readOnly = true)
    public StudentResponse loadProfile(Long userId) {
        return studentService.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("请先完善学生档案"));
    }

    @Transactional
    public StudentResponse saveProfile(Long userId, StudentProfileRequest request) {
        return studentRepository.findByUser_Id(userId)
                .map(existing -> studentService.update(existing.getId(),
                        new StudentUpdateRequest(request.name(), request.gender(), request.school(), request.major(),
                                request.grade(), request.education(), request.avatar())))
                .orElseGet(() -> studentService.create(new StudentCreateRequest(userId, request.name(), request.gender(),
                        request.school(), request.major(), request.grade(), request.education(), request.avatar())));
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> listResumes(Long userId) {
        TsukiStudent student = requireStudent(userId);
        return resumeService.findByStudentId(student.getId());
    }

    @Transactional
    public ResumeResponse createResume(Long userId, StudentResumeRequest request) {
        TsukiStudent student = requireStudent(userId);
        return resumeService.create(new com.example.campus.dto.resume.ResumeCreateRequest(student.getId(),
                request.title(), request.educationExperience(), request.workExperience(), request.skills(),
                request.selfEvaluation(), request.attachment()));
    }

    @Transactional
    public ResumeResponse updateResume(Long userId, Long resumeId, ResumeUpdateRequest request) {
        TsukiStudent student = requireStudent(userId);
        TsukiResume resume = requireResume(student.getId(), resumeId);
        return resumeService.update(resume.getId(), request);
    }

    @Transactional
    public void deleteResume(Long userId, Long resumeId) {
        TsukiStudent student = requireStudent(userId);
        TsukiResume resume = requireResume(student.getId(), resumeId);
        resumeService.delete(resume.getId());
    }

    @Transactional(readOnly = true)
    public List<com.example.campus.dto.job.JobResponse> searchJobs(String keyword, String companyName, String jobType,
            String location, String salaryRange) {
        return jobService.searchVisibleJobs(keyword, companyName, jobType, location, salaryRange);
    }

    @Transactional
    public ApplicationResponse apply(Long userId, StudentApplicationRequest request) {
        TsukiStudent student = requireStudent(userId);
        TsukiResume resume = requireResume(student.getId(), request.resumeId());
        TsukiJob job = jobRepository.findById(request.jobId())
                .orElseThrow(() -> new ResourceNotFoundException("未找到对应的职位"));
        applicationRepository.findByStudent_IdAndJob_Id(student.getId(), job.getId())
                .ifPresent(existing -> {
                    throw new IllegalStateException("您已投递该职位，请勿重复投递");
                });
        TsukiApplication application = TsukiApplication.builder()
                .student(student)
                .resume(resume)
                .job(job)
                .company(job.getCompany())
                .status("待查看")
                .build();
        TsukiApplication saved = applicationRepository.save(application);
        return applicationService.findById(saved.getId());
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> listApplications(Long userId) {
        TsukiStudent student = requireStudent(userId);
        return applicationService.findByStudentId(student.getId());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> listMessages(Long userId) {
        return messageService.findByReceiverId(userId);
    }

    @Transactional
    public MessageResponse markMessageRead(Long userId, Long messageId) {
        return messageRepository.findById(messageId)
                .filter(message -> message.getReceiver().getId().equals(userId))
                .map(message -> messageService.update(messageId, new MessageUpdateRequest(null, null, Boolean.TRUE)))
                .orElseThrow(() -> new ResourceNotFoundException("未找到对应的消息"));
    }

    @Transactional
    public String uploadAttachment(Long userId, MultipartFile file) {
        TsukiStudent student = requireStudent(userId);
        try {
            String publicPath = fileStorageService.store(file, FileStorageService.StorageArea.RESUME,
                    "resume-" + student.getId());
            resumeAttachmentService.recordUpload(student, file, publicPath);
            return publicPath;
        } catch (Exception ex) {
            throw new IllegalStateException("上传附件失败: " + ex.getMessage(), ex);
        }
    }

    @Transactional
    public ResumeResponse replaceAttachment(Long userId, Long resumeId, MultipartFile file) {
        TsukiStudent student = requireStudent(userId);
        TsukiResume resume = requireResume(student.getId(), resumeId);
        try {
            if (resume.getAttachment() != null) {
                fileStorageService.deleteIfExists(resume.getAttachment());
            }
            String path = fileStorageService.store(file, FileStorageService.StorageArea.RESUME,
                    "resume-" + resume.getId());
            resumeAttachmentService.recordUpload(student, file, path);
            resume.setAttachment(path);
            resumeRepository.save(resume);
            resumeAttachmentService.syncAttachment(student, resume, path, file);
            return resumeService.findById(resume.getId());
        } catch (Exception ex) {
            throw new IllegalStateException("更新附件失败: " + ex.getMessage(), ex);
        }
    }

    @Transactional(readOnly = true)
    public long countUnreadMessages(Long userId) {
        return messageRepository.countByReceiver_IdAndIsRead(userId, Boolean.FALSE);
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> listAnnouncements() {
        return announcementService.findByTargets(List.of("all", "student"));
    }

    @Transactional
    public DiscussionResponse createDiscussion(Long userId, DiscussionCreateRequest request) {
        return discussionService.create(userId, request);
    }

    private TsukiStudent requireStudent(Long userId) {
        return studentRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("请先完善学生档案"));
    }

    private TsukiResume requireResume(Long studentId, Long resumeId) {
        TsukiResume resume = resumeRepository.findById(resumeId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到简历信息"));
        if (!resume.getStudent().getId().equals(studentId)) {
            throw new IllegalArgumentException("无法操作其他同学的简历");
        }
        return resume;
    }
}
