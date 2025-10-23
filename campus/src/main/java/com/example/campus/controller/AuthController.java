package com.example.campus.controller;

import com.example.campus.dto.LoginRequest;
import com.example.campus.dto.LoginResponse;
import com.example.campus.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginResult result = authService.login(request);
        LoginResponse response = new LoginResponse(
                result.userId(),
                result.username(),
                "登录成功",
                result.role(),
                result.roleDisplayName(),
                result.redirectPath(),
                result.token());
        return ResponseEntity.ok(response);
    }
}
