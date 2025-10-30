package com.example.campus.dto.job;

import java.time.LocalDateTime;

public record JobResponse(
        Long id,
        Long companyId,
        String companyName,
        String jobTitle,
        String jobType,
        String salaryRange,
        String location,
        String requirement,
        String description,
        Integer durationMonths,
        String status,
        LocalDateTime createTime,
        LocalDateTime updateTime) {
}
