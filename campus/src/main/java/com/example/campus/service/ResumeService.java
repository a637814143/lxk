package com.example.campus.service;

import com.example.campus.dto.resume.ResumeCreateRequest;
import com.example.campus.dto.resume.ResumeResponse;
import com.example.campus.dto.resume.ResumeUpdateRequest;
import com.example.campus.entity.TsukiResume;
import com.example.campus.entity.TsukiStudent;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiResumeRepository;
import com.example.campus.repository.TsukiStudentRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ResumeService {

    private final TsukiResumeRepository resumeRepository;
    private final TsukiStudentRepository studentRepository;

    @Transactional(readOnly = true)
    public List<ResumeResponse> findAll() {
        return resumeRepository.findAll().stream()
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
        return toResponse(saved);
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
        return toResponse(resume);
    }

    @Transactional
    public void delete(Long id) {
        TsukiResume resume = getResume(id);
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
        return new ResumeResponse(
                resume.getId(),
                resume.getStudent().getId(),
                resume.getTitle(),
                resume.getEducationExperience(),
                resume.getWorkExperience(),
                resume.getSkills(),
                resume.getSelfEvaluation(),
                resume.getAttachment(),
                resume.getCreateTime(),
                resume.getUpdateTime());
    }
}
