package com.example.campus.dto.portal.admin;

import java.util.Map;

public record AdminDashboardSummary(
        long totalStudents,
        long totalCompanies,
        long pendingCompanies,
        long totalJobs,
        long approvedJobs,
        long pendingJobs,
        long totalApplications,
        Map<String, Long> statusBreakdown,
        long unreadMessages) {
}
