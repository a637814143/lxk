package com.example.campus.service;

import com.example.campus.dto.admin.AdminCreateRequest;
import com.example.campus.dto.admin.AdminResponse;
import com.example.campus.dto.admin.AdminUpdateRequest;
import com.example.campus.entity.TsukiAdmin;
import com.example.campus.entity.TsukiUser;
import com.example.campus.entity.UserRole;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiAdminRepository;
import com.example.campus.repository.TsukiUserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final TsukiAdminRepository adminRepository;
    private final TsukiUserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AdminResponse> findAll() {
        return adminRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<AdminResponse> findByUserId(Long userId) {
        return adminRepository.findByUser_Id(userId)
                .map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public AdminResponse findById(Long id) {
        return toResponse(getAdmin(id));
    }

    @Transactional
    public AdminResponse create(AdminCreateRequest request) {
        TsukiUser user = getUser(request.userId());
        TsukiAdmin admin = TsukiAdmin.builder()
                .user(user)
                .name(request.name())
                .level(request.level())
                .build();
        TsukiAdmin saved = adminRepository.save(admin);
        return toResponse(saved);
    }

    @Transactional
    public AdminResponse update(Long id, AdminUpdateRequest request) {
        TsukiAdmin admin = getAdmin(id);
        if (request.name() != null) {
            admin.setName(request.name());
        }
        if (request.level() != null) {
            admin.setLevel(request.level());
        }
        return toResponse(admin);
    }

    @Transactional
    public void delete(Long id) {
        TsukiAdmin admin = getAdmin(id);
        adminRepository.delete(admin);
    }

    private TsukiAdmin getAdmin(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的管理员"));
    }

    private TsukiUser getUser(Long userId) {
        TsukiUser user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + userId + "的用户"));
        if (user.getRole() != UserRole.ADMIN) {
            throw new IllegalArgumentException("用户角色必须为" + UserRole.ADMIN.getDisplayName());
        }
        return user;
    }

    private AdminResponse toResponse(TsukiAdmin admin) {
        return new AdminResponse(
                admin.getId(),
                admin.getUser().getId(),
                admin.getName(),
                admin.getLevel());
    }
}
