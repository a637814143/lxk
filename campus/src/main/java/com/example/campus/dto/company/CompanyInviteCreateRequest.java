package com.example.campus.dto.company;

import jakarta.validation.constraints.Size;

public record CompanyInviteCreateRequest(
        @Size(max = 255, message = "备注不能超过255个字符")
        String note,

        @Size(max = 100, message = "企业名称提示不能超过100个字符")
        String companyNameHint) {
}
