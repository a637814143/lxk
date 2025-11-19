package com.example.campus.dto.portal.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyInviteVerifyRequest(
        @NotBlank(message = "邀请码不能为空")
        @Size(max = 64, message = "邀请码不能超过64个字符")
        String inviteCode) {
}

