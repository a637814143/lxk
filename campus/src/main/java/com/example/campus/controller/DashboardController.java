package com.example.campus.controller;

import com.example.campus.dto.dashboard.AdminDashboardResponse;
import com.example.campus.dto.dashboard.CompanyDashboardResponse;
import com.example.campus.dto.dashboard.StudentDashboardResponse;
import com.example.campus.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/student/{userId}")
    public StudentDashboardResponse studentDashboard(@PathVariable Long userId) {
        return dashboardService.loadStudentDashboard(userId);
    }

    @GetMapping("/company/{userId}")
    public CompanyDashboardResponse companyDashboard(@PathVariable Long userId) {
        return dashboardService.loadCompanyDashboard(userId);
    }

    @GetMapping("/admin/{userId}")
    public AdminDashboardResponse adminDashboard(@PathVariable Long userId) {
        return dashboardService.loadAdminDashboard(userId);
    }
}
