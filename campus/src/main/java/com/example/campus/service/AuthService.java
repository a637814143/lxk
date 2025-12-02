package com.example.campus.service;

import com.example.campus.dto.LoginRequest;
import com.example.campus.dto.RegisterRequest;
import com.example.campus.dto.admin.AdminCreateRequest;
import com.example.campus.dto.student.StudentCreateRequest;
import com.example.campus.entity.TsukiUser;
import com.example.campus.entity.UserRole;
import com.example.campus.exception.InvalidCredentialsException;
import com.example.campus.exception.UserAlreadyExistsException;
import com.example.campus.repository.TsukiUserRepository;
import com.example.campus.security.JwtService;
import com.example.campus.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final TsukiUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final StudentService studentService;
    private final AdminService adminService;

    @Transactional
    public void register(RegisterRequest request) {
        String username = normalize(request.username());
        validateUsername(username);
        String email = normalize(request.email());
        validateEmail(email);
        String phone = normalizeNullable(request.phone());
        String displayName = normalizeNullable(request.displayName());
        String companyName = normalizeNullable(request.companyName());
        String inviteCode = normalizeNullable(request.inviteCode());
        UserRole role = request.role();

        validateRoleDetails(role, displayName, companyName, inviteCode);

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
                .role(role)
                .status(1)
                .build();

        TsukiUser savedUser = userRepository.save(user);
        initializeRoleProfile(savedUser, role, displayName);
    }

    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest request) {
        return doLogin(request, false);
    }

    @Transactional(readOnly = true)
    public LoginResult loginAdmin(LoginRequest request) {
        return doLogin(request, true);
    }

    private LoginResult doLogin(LoginRequest request, boolean adminOnly) {
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
        if (adminOnly && role != UserRole.ADMIN) {
            throw new InvalidCredentialsException("当前系统仅支持系统管理员登录");
        }

        UserPrincipal principal = UserPrincipal.fromEntity(user);
        String token = jwtService.generateToken(principal);
        return new LoginResult(user.getId(), username, role, role.getDisplayName(), role.getRedirectPath(), token);
    }

    private void validateRoleDetails(UserRole role, String displayName, String companyName, String inviteCode) {
        if (role == null) {
            throw new IllegalArgumentException("用户角色不能为空");
        }
        switch (role) {
            case STUDENT -> {
                if (displayName == null || displayName.isEmpty()) {
                    throw new IllegalArgumentException("学生姓名不能为空");
                }
            }
            case COMPANY -> {
            }
            case ADMIN -> {
                throw new IllegalArgumentException("管理员账号仅限系统预置，禁止自行注册");
            }
            default -> {
            }
        }
    }

    private void initializeRoleProfile(TsukiUser user, UserRole role, String displayName) {
        Long userId = user.getId();
        switch (role) {
            case STUDENT -> createStudentProfile(userId, displayName);
            case COMPANY -> {
            }
            case ADMIN -> {
            }
            default -> {
            }
        }
    }

    private void createStudentProfile(Long userId, String displayName) {
        if (studentService.findByUserId(userId).isPresent()) {
            return;
        }
        studentService.create(new StudentCreateRequest(userId, displayName, null, null, null, null, null, null));
    }

    private void createAdminProfile(Long userId, String displayName) {
        if (adminService.findByUserId(userId).isPresent()) {
            return;
        }
        adminService.create(new AdminCreateRequest(userId, displayName, 1));
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

    public record LoginResult(Long userId, String username, UserRole role, String roleDisplayName, String redirectPath,
                              String token) {
    }
}
