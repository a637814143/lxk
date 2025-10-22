package com.example.campus.dto.dashboard;

import com.example.campus.dto.application.ApplicationResponse;
import com.example.campus.dto.company.CompanyResponse;
import com.example.campus.dto.job.JobResponse;
import com.example.campus.dto.message.MessageResponse;
import com.example.campus.dto.resume.ResumeResponse;
import com.example.campus.dto.student.StudentResponse;
import com.example.campus.dto.user.UserResponse;
import java.util.List;

public record CompanyDashboardResponse(
        UserResponse user,
        CompanyResponse company,
        List<JobResponse> jobs,
        List<ApplicationResponse> applications,
        List<MessageResponse> sentMessages,
        List<MessageResponse> inboxMessages,
        List<StudentResponse> applicants,
        List<ResumeResponse> resumes) {
}
