package com.example.campus.dto.portal.admin;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CompanyInviteCreateRequest(
        @Size(max = 64, message = "邀请码长度不能超过64个字符")
        @Pattern(regexp = "^$|^[A-Za-z0-9-]{6,64}$", message = "邀请码需为6-64位字母、数字或连字符")
        String code,

        @Size(max = 255, message = "备注不能超过255个字符")
        String note,

        @Size(max = 100, message = "企业名称提示不能超过100个字符")
        String companyNameHint) {
}
