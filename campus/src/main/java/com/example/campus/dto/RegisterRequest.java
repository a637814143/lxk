package com.example.campus.dto;

import com.example.campus.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "用户名不能为空")
        @Size(max = 50, message = "用户名不能超过50个字符")
        String username,

        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 64, message = "密码长度需在6-64个字符之间")
        String password,

        @NotBlank(message = "邮箱不能为空")
        @Email(message = "邮箱格式不正确")
        @Size(max = 100, message = "邮箱不能超过100个字符")
        String email,

        @Size(max = 20, message = "手机号不能超过20个字符")
        @Pattern(regexp = "^$|^\\+?[0-9\\-]{5,20}$", message = "手机号格式不正确")
        String phone,

        @NotNull(message = "用户角色不能为空")
        UserRole role,

        @Size(max = 50, message = "姓名不能超过50个字符")
        String displayName,

        @Size(max = 100, message = "企业名称不能超过100个字符")
        String companyName,

        @Size(max = 64, message = "邀请码不能超过64个字符")
        String inviteCode) {
}
