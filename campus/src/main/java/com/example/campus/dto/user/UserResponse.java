package com.example.campus.dto.user;

import com.example.campus.entity.UserRole;
import java.time.LocalDateTime;

public record UserResponse(
        Long id,
        String username,
        String email,
        String phone,
        UserRole role,
        Integer status,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
