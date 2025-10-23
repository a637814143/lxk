package com.example.campus.dto.company;

import jakarta.validation.constraints.Size;

public record CompanyUpdateRequest(
        @Size(max = 100, message = "企业名称不能超过100个字符")
        String companyName,

        @Size(max = 100, message = "营业执照号不能超过100个字符")
        String licenseNumber,

        @Size(max = 100, message = "行业不能超过100个字符")
        String industry,

        @Size(max = 255, message = "地址不能超过255个字符")
        String address,

        @Size(max = 255, message = "官网链接不能超过255个字符")
        String website,

        String description,

        @Size(max = 255, message = "Logo链接不能超过255个字符")
        String logo,

        @Size(max = 20, message = "审核状态不能超过20个字符")
        String auditStatus,

        @Size(max = 255, message = "审核备注不能超过255个字符")
        String auditReason) {
}
