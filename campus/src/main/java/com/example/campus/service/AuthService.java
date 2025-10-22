package com.example.campus.service;

import com.example.campus.dto.LoginRequest;
import com.example.campus.dto.RegisterRequest;
import com.example.campus.entity.TsukiUser;
import com.example.campus.entity.UserRole;
import com.example.campus.exception.InvalidCredentialsException;
import com.example.campus.exception.UserAlreadyExistsException;
import com.example.campus.repository.TsukiUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TsukiUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {
        String username = normalize(request.username());
        validateUsername(username);
        String email = normalize(request.email());
        validateEmail(email);
        String phone = normalizeNullable(request.phone());

        userRepository.findByUsernameIgnoreCase(username)
                .ifPresent(existing -> {
                    throw new UserAlreadyExistsException("用户名已存在，请直接登录");
                });
        userRepository.findByEmailIgnoreCase(email)
                .ifPresent(existing -> {
                    throw new UserAlreadyExistsException("邮箱已注册，请直接登录");
                });

        TsukiUser user = TsukiUser.builder()
                .username(username)
                .password(passwordEncoder.encode(request.password()))
                .email(email)
                .phone(phone)
                .role(request.role())
                .status(1)
                .build();

        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest request) {
        String username = normalize(request.username());
        validateUsername(username);

        TsukiUser user = userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new InvalidCredentialsException("用户名或密码错误"));

        Integer status = user.getStatus();
        if (status != null && status == 0) {
            throw new InvalidCredentialsException("账户已被禁用，请联系管理员");
        }

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException("用户名或密码错误");
        }

        UserRole role = user.getRole();
        return new LoginResult(user.getId(), username, role, role.getDisplayName(), role.getRedirectPath());
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    private String normalizeNullable(String value) {
        String normalized = normalize(value);
        return (normalized == null || normalized.isEmpty()) ? null : normalized;
    }

    private void validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("邮箱不能为空");
        }
    }

    public record LoginResult(Long userId, String username, UserRole role, String roleDisplayName, String redirectPath) {
    }
}
