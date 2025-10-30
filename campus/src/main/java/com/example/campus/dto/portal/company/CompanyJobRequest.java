package com.example.campus.dto.portal.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CompanyJobRequest(
        @NotBlank(message = "职位名称不能为空")
        @Size(max = 100, message = "职位名称不能超过100个字符")
        String jobTitle,

        @Size(max = 50, message = "职位类别不能超过50个字符")
        String jobType,

        @Size(max = 50, message = "薪资范围不能超过50个字符")
        String salaryRange,

        @Size(max = 100, message = "工作地点不能超过100个字符")
        String location,

        String requirement,

        String description,

        @Positive(message = "合同时长必须为正数（单位：月）")
        Integer durationMonths) {
}
