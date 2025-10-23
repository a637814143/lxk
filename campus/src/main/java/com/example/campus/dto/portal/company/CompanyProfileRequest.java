package com.example.campus.dto.portal.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyProfileRequest(
        @NotBlank(message = "企业名称不能为空")
        @Size(max = 100, message = "企业名称不能超过100个字符")
        String companyName,

        @Size(max = 100, message = "营业执照号不能超过100个字符")
        String licenseNumber,

        @Size(max = 100, message = "行业类别不能超过100个字符")
        String industry,

        @Size(max = 255, message = "企业地址不能超过255个字符")
        String address,

        @Size(max = 255, message = "企业官网不能超过255个字符")
        String website,

        String description,

        @Size(max = 255, message = "Logo链接不能超过255个字符")
        String logo) {
}
