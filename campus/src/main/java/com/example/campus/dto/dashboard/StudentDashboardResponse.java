package com.example.campus.dto.dashboard;

import com.example.campus.dto.application.ApplicationResponse;
import com.example.campus.dto.job.JobResponse;
import com.example.campus.dto.message.MessageResponse;
import com.example.campus.dto.resume.ResumeResponse;
import com.example.campus.dto.student.StudentResponse;
import com.example.campus.dto.user.UserResponse;
import java.util.List;

public record StudentDashboardResponse(
        UserResponse user,
        StudentResponse profile,
        List<ResumeResponse> resumes,
        List<ApplicationResponse> applications,
        List<JobResponse> jobs,
        List<MessageResponse> messages) {
}
