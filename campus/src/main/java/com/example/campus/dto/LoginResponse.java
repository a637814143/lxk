package com.example.campus.dto;

import com.example.campus.entity.UserRole;

public record LoginResponse(
        Long userId,
        String username,
        String message,
        UserRole role,
        String roleDisplayName,
        String redirectPath) {
}
