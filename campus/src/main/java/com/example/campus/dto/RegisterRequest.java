package com.example.campus.dto;

import com.example.campus.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 64, message = "用户名不能超过64个字符")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度需在6-64个字符之间")
        String password,

        @NotNull(message = "用户角色不能为空")
        UserRole role) {
}
