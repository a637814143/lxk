package com.example.campus.dto.user;

import com.example.campus.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @Size(max = 50, message = "用户名不能超过50个字符")
        String username,

        @Size(min = 6, max = 64, message = "密码长度需在6-64个字符之间")
        String password,

        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱不能超过100个字符")
        String email,

        @Size(max = 20, message = "手机号不能超过20个字符")
        @Pattern(regexp = "^$|^\\+?[0-9\\-]{5,20}$", message = "手机号格式不正确")
        String phone,

        UserRole role,

        Integer status) {
}
