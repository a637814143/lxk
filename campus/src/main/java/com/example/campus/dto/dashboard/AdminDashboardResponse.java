package com.example.campus.dto.dashboard;

import com.example.campus.dto.admin.AdminResponse;
import com.example.campus.dto.application.ApplicationResponse;
import com.example.campus.dto.announcement.AnnouncementResponse;
import com.example.campus.dto.audit.AuditLogResponse;
import com.example.campus.dto.company.CompanyResponse;
import com.example.campus.dto.job.JobResponse;
import com.example.campus.dto.message.MessageResponse;
import com.example.campus.dto.resume.ResumeResponse;
import com.example.campus.dto.student.StudentResponse;
import com.example.campus.dto.user.UserResponse;
import java.util.List;

public record AdminDashboardResponse(
        UserResponse user,
        AdminResponse profile,
        List<UserResponse> users,
        List<AdminResponse> admins,
        List<CompanyResponse> companies,
        List<StudentResponse> students,
        List<JobResponse> jobs,
        List<ResumeResponse> resumes,
        List<ApplicationResponse> applications,
        List<AnnouncementResponse> announcements,
        List<AuditLogResponse> auditLogs,
        List<MessageResponse> inboxMessages,
        List<MessageResponse> sentMessages,
        List<MessageResponse> allMessages) {
}
