package com.example.campus.service;

import com.example.campus.dto.LoginRequest;
import com.example.campus.dto.RegisterRequest;
import com.example.campus.entity.UserAccount;
import com.example.campus.entity.UserRole;
import com.example.campus.exception.InvalidCredentialsException;
import com.example.campus.exception.UserAlreadyExistsException;
import com.example.campus.repository.UserAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(RegisterRequest request) {
        String username = normalizeUsername(request.username());
        validateUsername(username);
        userAccountRepository.findByUsernameIgnoreCase(username)
                .ifPresent(existing -> {
                    throw new UserAlreadyExistsException("用户已存在，请直接登录");
                });

        UserAccount userAccount = UserAccount.builder()
                .username(username)
                .password(passwordEncoder.encode(request.password()))
                .role(request.role())
                .build();

        userAccountRepository.save(userAccount);
    }

    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest request) {
        String username = normalizeUsername(request.username());
        validateUsername(username);
        UserAccount userAccount = userAccountRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new InvalidCredentialsException("用户名或密码错误"));

        if (!passwordEncoder.matches(request.password(), userAccount.getPassword())) {
            throw new InvalidCredentialsException("用户名或密码错误");
        }

        UserRole role = userAccount.getRole();
        return new LoginResult(username, role, role.getDisplayName(), role.getRedirectPath());
    }

    private String normalizeUsername(String username) {
        return username == null ? null : username.trim();
    }

    private void validateUsername(String username) {
        if (username == null || username.isEmpty()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
    }

    public record LoginResult(String username, UserRole role, String roleDisplayName, String redirectPath) {
    }
}
