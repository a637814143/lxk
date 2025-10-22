package com.example.campus.dto.job;

import jakarta.validation.constraints.Size;

public record JobUpdateRequest(
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

        @Size(max = 20, message = "职位状态不能超过20个字符")
        String status) {
}
