package com.example.campus.service;

import com.example.campus.dto.resume.ResumeAttachmentResponse;
import com.example.campus.dto.resume.ResumeCreateRequest;
import com.example.campus.dto.resume.ResumeResponse;
import com.example.campus.dto.resume.ResumeUpdateRequest;
import com.example.campus.entity.TsukiResume;
import com.example.campus.entity.TsukiResumeAttachment;
import com.example.campus.entity.TsukiStudent;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiResumeRepository;
import com.example.campus.repository.TsukiResumeAttachmentRepository;
import com.example.campus.repository.TsukiStudentRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final TsukiResumeRepository resumeRepository;
    private final TsukiStudentRepository studentRepository;
    private final TsukiResumeAttachmentRepository attachmentRepository;

    @Transactional(readOnly = true)
    public List<ResumeResponse> findAll() {
        return resumeRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<ResumeResponse> findByStudentId(Long studentId) {
        return resumeRepository.findByStudent_Id(studentId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ResumeResponse findById(Long id) {
        return toResponse(getResume(id));
    }

    @Transactional
    public ResumeResponse create(ResumeCreateRequest request) {
        TsukiStudent student = getStudent(request.studentId());
        TsukiResume resume = TsukiResume.builder()
                .student(student)
                .title(request.title())
                .educationExperience(request.educationExperience())
                .workExperience(request.workExperience())
                .skills(request.skills())
                .selfEvaluation(request.selfEvaluation())
                .attachment(request.attachment())
                .build();
        TsukiResume saved = resumeRepository.save(resume);
        syncAttachments(saved, request.attachment(), request.attachmentId());
        return toResponse(fetchWithAttachments(saved.getId()));
    }

    @Transactional
    public ResumeResponse update(Long id, ResumeUpdateRequest request) {
        TsukiResume resume = getResume(id);
        if (request.title() != null) {
            resume.setTitle(request.title());
        }
        if (request.educationExperience() != null) {
            resume.setEducationExperience(request.educationExperience());
        }
        if (request.workExperience() != null) {
            resume.setWorkExperience(request.workExperience());
        }
        if (request.skills() != null) {
            resume.setSkills(request.skills());
        }
        if (request.selfEvaluation() != null) {
            resume.setSelfEvaluation(request.selfEvaluation());
        }
        if (request.attachment() != null) {
            resume.setAttachment(request.attachment());
        }
        TsukiResume saved = resumeRepository.save(resume);
        syncAttachments(saved, request.attachment(), request.attachmentId());
        return toResponse(fetchWithAttachments(saved.getId()));
    }

    @Transactional
    public void delete(Long id) {
        TsukiResume resume = getResume(id);
        attachmentRepository.findByResume_Id(resume.getId())
                .forEach(attachmentRepository::delete);
        resumeRepository.delete(resume);
    }

    private TsukiResume getResume(Long id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的简历"));
    }

    private TsukiStudent getStudent(Long studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + studentId + "的学生"));
    }

    private ResumeResponse toResponse(TsukiResume resume) {
        List<ResumeAttachmentResponse> attachments = resume.getAttachments().stream()
                .map(this::toAttachmentResponse)
                .collect(Collectors.toList());
        return new ResumeResponse(
                resume.getId(),
                resume.getStudent().getId(),
                resume.getTitle(),
                resume.getEducationExperience(),
                resume.getWorkExperience(),
                resume.getSkills(),
                resume.getSelfEvaluation(),
                resume.getAttachment(),
                attachments,
                resume.getCreateTime(),
                resume.getUpdateTime());
    }

    private ResumeAttachmentResponse toAttachmentResponse(TsukiResumeAttachment attachment) {
        return new ResumeAttachmentResponse(
                attachment.getId(),
                attachment.getFileName(),
                attachment.getFilePath(),
                attachment.getContentType(),
                attachment.getFileSize(),
                attachment.getUploadedAt());
    }

    private TsukiResume fetchWithAttachments(Long id) {
        return resumeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的简历"));
    }

    private void syncAttachments(TsukiResume resume, String attachmentPath, Long attachmentId) {
        List<TsukiResumeAttachment> existing = attachmentRepository.findByResume_Id(resume.getId());
        TsukiResumeAttachment target = null;
        if (attachmentId != null) {
            target = attachmentRepository.findById(attachmentId).orElse(null);
        }
        if (target == null && StringUtils.hasText(attachmentPath)) {
            target = attachmentRepository.findByFilePath(attachmentPath).orElse(null);
        }
        if (target != null && !target.getStudent().getId().equals(resume.getStudent().getId())) {
            target = null;
        }
        if (!StringUtils.hasText(attachmentPath)) {
            existing.forEach(attachmentRepository::delete);
            resume.setAttachment(null);
            return;
        }
        if (target == null) {
            target = TsukiResumeAttachment.builder()
                    .resume(resume)
                    .student(resume.getStudent())
                    .fileName(extractFileName(attachmentPath))
                    .filePath(attachmentPath)
                    .build();
        } else {
            target.setResume(resume);
            target.setStudent(resume.getStudent());
            target.setFilePath(attachmentPath);
        }
        TsukiResumeAttachment saved = attachmentRepository.save(target);
        for (TsukiResumeAttachment attachment : existing) {
            if (!attachment.getId().equals(saved.getId())) {
                attachmentRepository.delete(attachment);
            }
        }
        resume.setAttachment(attachmentPath);
    }

    private String extractFileName(String path) {
        if (!StringUtils.hasText(path)) {
            return "附件简历";
        }
        int index = path.lastIndexOf('/');
        if (index >= 0 && index < path.length() - 1) {
            return path.substring(index + 1);
        }
        return path;
    }
}
