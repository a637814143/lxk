package com.example.campus.service;

import com.example.campus.dto.application.ApplicationCreateRequest;
import com.example.campus.dto.application.ApplicationResponse;
import com.example.campus.dto.application.ApplicationUpdateRequest;
import com.example.campus.entity.TsukiApplication;
import com.example.campus.entity.TsukiCompany;
import com.example.campus.entity.TsukiJob;
import com.example.campus.entity.TsukiResume;
import com.example.campus.entity.TsukiStudent;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiApplicationRepository;
import com.example.campus.repository.TsukiCompanyRepository;
import com.example.campus.repository.TsukiJobRepository;
import com.example.campus.repository.TsukiResumeRepository;
import com.example.campus.repository.TsukiStudentRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private static final Set<String> ALLOWED_STATUSES =
            Set.of("待查阅", "已查阅", "面试中", "录用", "拒绝");

    private final TsukiApplicationRepository applicationRepository;
    private final TsukiStudentRepository studentRepository;
    private final TsukiResumeRepository resumeRepository;
    private final TsukiJobRepository jobRepository;
    private final TsukiCompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findAll() {
        return applicationRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findByStudentId(Long studentId) {
        return applicationRepository.findByStudent_Id(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findByCompanyId(Long companyId) {
        return applicationRepository.findByCompany_Id(companyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ApplicationResponse findById(Long id) {
        return toResponse(getApplication(id));
    }

    @Transactional
    public ApplicationResponse create(ApplicationCreateRequest request) {
        TsukiStudent student = getStudent(request.studentId());
        TsukiResume resume = getResume(request.resumeId());
        TsukiJob job = getJob(request.jobId());
        TsukiCompany company = getCompany(request.companyId());
        TsukiApplication application = TsukiApplication.builder()
                .student(student)
                .resume(resume)
                .job(job)
                .company(company)
                .status(normalizeStatus(request.status()))
                .decisionNote(request.decisionNote())
                .build();
        TsukiApplication saved = applicationRepository.save(application);
        return toResponse(saved);
    }

    @Transactional
    public ApplicationResponse update(Long id, ApplicationUpdateRequest request) {
        TsukiApplication application = getApplication(id);
        if (request.status() != null) {
            application.setStatus(normalizeStatus(request.status()));
        }
        if (request.decisionNote() != null) {
            application.setDecisionNote(request.decisionNote());
        }
        return toResponse(application);
    }

    @Transactional
    public void delete(Long id) {
        TsukiApplication application = getApplication(id);
        applicationRepository.delete(application);
    }

    private TsukiApplication getApplication(Long id) {
        return applicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found: " + id));
    }

    private TsukiStudent getStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    private TsukiResume getResume(Long id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Resume not found: " + id));
    }

    private TsukiJob getJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found: " + id));
    }

    private TsukiCompany getCompany(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found: " + id));
    }

    private ApplicationResponse toResponse(TsukiApplication application) {
        return new ApplicationResponse(
                application.getId(),
                application.getStudent().getId(),
                application.getResume().getId(),
                application.getJob().getId(),
                application.getCompany().getId(),
                application.getStatus(),
                application.getDecisionNote(),
                application.getApplyTime(),
                application.getUpdateTime());
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "待查阅";
        }
        if (!ALLOWED_STATUSES.contains(status)) {
            throw new IllegalArgumentException("投递状态仅支持: " + String.join("/", ALLOWED_STATUSES));
        }
        return status;
    }
}
