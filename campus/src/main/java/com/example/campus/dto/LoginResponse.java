package com.example.campus.dto;

import com.example.campus.entity.UserRole;

public record LoginResponse(
        String message,
        UserRole role,
        String roleDisplayName,
        String redirectPath) {
}
