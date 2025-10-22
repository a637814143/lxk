package com.example.campus.service;

import com.example.campus.dto.student.StudentCreateRequest;
import com.example.campus.dto.student.StudentResponse;
import com.example.campus.dto.student.StudentUpdateRequest;
import com.example.campus.entity.TsukiStudent;
import com.example.campus.entity.TsukiUser;
import com.example.campus.entity.UserRole;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiStudentRepository;
import com.example.campus.repository.TsukiUserRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentService {

    private static final Set<String> ALLOWED_GENDERS = Set.of("男", "女", "其他");

    private final TsukiStudentRepository studentRepository;
    private final TsukiUserRepository userRepository;

    @Transactional(readOnly = true)
    public List<StudentResponse> findAll() {
        return studentRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudentResponse findById(Long id) {
        return toResponse(getStudent(id));
    }

    @Transactional
    public StudentResponse create(StudentCreateRequest request) {
        TsukiUser user = getUser(request.userId(), UserRole.STUDENT);
        TsukiStudent student = TsukiStudent.builder()
                .user(user)
                .name(request.name())
                .gender(normalizeGender(request.gender()))
                .school(request.school())
                .major(request.major())
                .grade(request.grade())
                .education(request.education())
                .avatar(request.avatar())
                .build();
        TsukiStudent saved = studentRepository.save(student);
        return toResponse(saved);
    }

    @Transactional
    public StudentResponse update(Long id, StudentUpdateRequest request) {
        TsukiStudent student = getStudent(id);
        if (request.name() != null) {
            student.setName(request.name());
        }
        if (request.gender() != null) {
            student.setGender(normalizeGender(request.gender()));
        }
        if (request.school() != null) {
            student.setSchool(request.school());
        }
        if (request.major() != null) {
            student.setMajor(request.major());
        }
        if (request.grade() != null) {
            student.setGrade(request.grade());
        }
        if (request.education() != null) {
            student.setEducation(request.education());
        }
        if (request.avatar() != null) {
            student.setAvatar(request.avatar());
        }
        return toResponse(student);
    }

    @Transactional
    public void delete(Long id) {
        TsukiStudent student = getStudent(id);
        studentRepository.delete(student);
    }

    private TsukiStudent getStudent(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的学生"));
    }

    private TsukiUser getUser(Long userId, UserRole requiredRole) {
        TsukiUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + userId + "的用户"));
        if (requiredRole != null && user.getRole() != requiredRole) {
            throw new IllegalArgumentException("用户角色必须为" + requiredRole.getDisplayName());
        }
        return user;
    }

    private StudentResponse toResponse(TsukiStudent student) {
        return new StudentResponse(
                student.getId(),
                student.getUser().getId(),
                student.getName(),
                student.getGender(),
                student.getSchool(),
                student.getMajor(),
                student.getGrade(),
                student.getEducation(),
                student.getAvatar());
    }

    private String normalizeGender(String gender) {
        if (gender == null || gender.isBlank()) {
            return null;
        }
        if (!ALLOWED_GENDERS.contains(gender)) {
            throw new IllegalArgumentException("性别取值仅支持: " + String.join("/", ALLOWED_GENDERS));
        }
        return gender;
    }
}
