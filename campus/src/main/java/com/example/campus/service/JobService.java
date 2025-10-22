package com.example.campus.service;

import com.example.campus.dto.job.JobCreateRequest;
import com.example.campus.dto.job.JobResponse;
import com.example.campus.dto.job.JobUpdateRequest;
import com.example.campus.entity.TsukiCompany;
import com.example.campus.entity.TsukiJob;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiCompanyRepository;
import com.example.campus.repository.TsukiJobRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JobService {

    private static final Set<String> ALLOWED_STATUSES = Set.of("pending", "approved", "rejected", "closed");

    private final TsukiJobRepository jobRepository;
    private final TsukiCompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public List<JobResponse> findAll() {
        return jobRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<JobResponse> findByCompanyId(Long companyId) {
        return jobRepository.findByCompany_Id(companyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public JobResponse findById(Long id) {
        return toResponse(getJob(id));
    }

    @Transactional
    public JobResponse create(JobCreateRequest request) {
        TsukiCompany company = getCompany(request.companyId());
        TsukiJob job = TsukiJob.builder()
                .company(company)
                .jobTitle(request.jobTitle())
                .jobType(request.jobType())
                .salaryRange(request.salaryRange())
                .location(request.location())
                .requirement(request.requirement())
                .description(request.description())
                .status(normalizeStatus(request.status()))
                .build();
        TsukiJob saved = jobRepository.save(job);
        return toResponse(saved);
    }

    @Transactional
    public JobResponse update(Long id, JobUpdateRequest request) {
        TsukiJob job = getJob(id);
        if (request.jobTitle() != null) {
            job.setJobTitle(request.jobTitle());
        }
        if (request.jobType() != null) {
            job.setJobType(request.jobType());
        }
        if (request.salaryRange() != null) {
            job.setSalaryRange(request.salaryRange());
        }
        if (request.location() != null) {
            job.setLocation(request.location());
        }
        if (request.requirement() != null) {
            job.setRequirement(request.requirement());
        }
        if (request.description() != null) {
            job.setDescription(request.description());
        }
        if (request.status() != null) {
            job.setStatus(normalizeStatus(request.status()));
        }
        return toResponse(job);
    }

    @Transactional
    public void delete(Long id) {
        TsukiJob job = getJob(id);
        jobRepository.delete(job);
    }

    private TsukiJob getJob(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的职位"));
    }

    private TsukiCompany getCompany(Long companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + companyId + "的企业"));
    }

    private JobResponse toResponse(TsukiJob job) {
        return new JobResponse(
                job.getId(),
                job.getCompany().getId(),
                job.getJobTitle(),
                job.getJobType(),
                job.getSalaryRange(),
                job.getLocation(),
                job.getRequirement(),
                job.getDescription(),
                job.getStatus(),
                job.getCreateTime(),
                job.getUpdateTime());
    }

    private String normalizeStatus(String status) {
        if (status == null || status.isBlank()) {
            return "pending";
        }
        String normalized = status.toLowerCase();
        if (!ALLOWED_STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("职位状态仅支持: " + String.join("/", ALLOWED_STATUSES));
        }
        return normalized;
    }
}
