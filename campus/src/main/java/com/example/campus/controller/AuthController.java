package com.example.campus.controller;

import com.example.campus.dto.LoginRequest;
import com.example.campus.dto.LoginResponse;
import com.example.campus.dto.MessageResponse;
import com.example.campus.dto.RegisterRequest;
import com.example.campus.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("注册成功，请登录"));
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthService.LoginResult result = authService.login(request);
        LoginResponse response = new LoginResponse(
                "登录成功",
                result.role(),
                result.roleDisplayName(),
                result.redirectPath());
        return ResponseEntity.ok(response);
    }
}
