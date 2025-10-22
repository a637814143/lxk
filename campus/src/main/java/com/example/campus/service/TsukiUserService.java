package com.example.campus.service;

import com.example.campus.dto.user.UserCreateRequest;
import com.example.campus.dto.user.UserResponse;
import com.example.campus.dto.user.UserUpdateRequest;
import com.example.campus.entity.TsukiUser;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiUserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TsukiUserService {

    private final TsukiUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        return toResponse(getUser(id));
    }

    @Transactional
    public UserResponse create(UserCreateRequest request) {
        String username = normalize(request.username());
        String email = normalize(request.email());
        String phone = normalizeNullable(request.phone());

        userRepository.findByUsernameIgnoreCase(username)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("用户名已存在");
                });

        userRepository.findByEmailIgnoreCase(email)
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("邮箱已存在");
                });

        TsukiUser user = TsukiUser.builder()
                .username(username)
                .password(passwordEncoder.encode(request.password()))
                .email(email)
                .phone(phone)
                .role(request.role())
                .status(request.status() == null ? 1 : request.status())
                .build();
        TsukiUser saved = userRepository.save(user);
        return toResponse(saved);
    }

    @Transactional
    public UserResponse update(Long id, UserUpdateRequest request) {
        TsukiUser user = getUser(id);
        if (request.username() != null) {
            String username = normalize(request.username());
            userRepository.findByUsernameIgnoreCase(username)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("用户名已存在");
                    });
            user.setUsername(username);
        }
        if (request.password() != null) {
            user.setPassword(passwordEncoder.encode(request.password()));
        }
        if (request.email() != null) {
            String email = normalize(request.email());
            userRepository.findByEmailIgnoreCase(email)
                    .filter(existing -> !existing.getId().equals(id))
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("邮箱已存在");
                    });
            user.setEmail(email);
        }
        if (request.phone() != null) {
            user.setPhone(normalizeNullable(request.phone()));
        }
        if (request.role() != null) {
            user.setRole(request.role());
        }
        if (request.status() != null) {
            user.setStatus(request.status());
        }
        return toResponse(user);
    }

    @Transactional
    public void delete(Long id) {
        TsukiUser user = getUser(id);
        userRepository.delete(user);
    }

    private TsukiUser getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的用户"));
    }

    private UserResponse toResponse(TsukiUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.getCreateTime(),
                user.getUpdateTime());
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("字段不能为空");
        }
        return normalized;
    }

    private String normalizeNullable(String value) {
        String normalized = normalize(value);
        return (normalized == null || normalized.isEmpty()) ? null : normalized;
    }
}
