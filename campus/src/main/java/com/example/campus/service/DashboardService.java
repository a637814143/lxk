package com.example.campus.service;

import com.example.campus.dto.backup.BackupResponse;
import com.example.campus.dto.dashboard.AdminDashboardResponse;
import com.example.campus.dto.dashboard.CompanyDashboardResponse;
import com.example.campus.dto.dashboard.StudentDashboardResponse;
import com.example.campus.dto.admin.AdminResponse;
import com.example.campus.dto.application.ApplicationResponse;
import com.example.campus.dto.announcement.AnnouncementResponse;
import com.example.campus.dto.audit.AuditLogResponse;
import com.example.campus.dto.company.CompanyResponse;
import com.example.campus.dto.discussion.DiscussionResponse;
import com.example.campus.dto.job.JobResponse;
import com.example.campus.dto.message.MessageResponse;
import com.example.campus.dto.resume.ResumeResponse;
import com.example.campus.dto.student.StudentResponse;
import com.example.campus.dto.user.UserResponse;
import com.example.campus.dto.finance.FinancialTransactionResponse;
import com.example.campus.entity.UserRole;
import com.example.campus.exception.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TsukiUserService userService;
    private final StudentService studentService;
    private final ResumeService resumeService;
    private final ApplicationService applicationService;
    private final JobService jobService;
    private final MessageService messageService;
    private final CompanyService companyService;
    private final AdminService adminService;
    private final AnnouncementService announcementService;
    private final AuditLogService auditLogService;
    private final FinancialTransactionService financialTransactionService;
    private final DataBackupService dataBackupService;
    private final DiscussionService discussionService;

    public StudentDashboardResponse loadStudentDashboard(Long userId) {
        UserResponse user = userService.findById(userId);
        ensureRole(user, UserRole.STUDENT);

        StudentResponse profile = studentService.findByUserId(userId).orElse(null);
        List<ResumeResponse> resumes = profile != null
                ? resumeService.findByStudentId(profile.id())
                : List.of();
        List<ApplicationResponse> applications = profile != null
                ? applicationService.findByStudentId(profile.id())
                : List.of();

        List<JobResponse> jobs = jobService.findAll();
        List<MessageResponse> messages = messageService.findByReceiverId(userId);

        return new StudentDashboardResponse(
                user,
                profile,
                List.copyOf(resumes),
                List.copyOf(applications),
                List.copyOf(jobs),
                List.copyOf(messages));
    }

    public CompanyDashboardResponse loadCompanyDashboard(Long userId) {
        UserResponse user = userService.findById(userId);
        ensureRole(user, UserRole.COMPANY);

        CompanyResponse company = companyService.findByUserId(userId).orElse(null);
        List<JobResponse> jobs = company != null
                ? jobService.findByCompanyId(company.id())
                : List.of();
        List<ApplicationResponse> applications = company != null
                ? applicationService.findByCompanyId(company.id())
                : List.of();

        List<MessageResponse> sentMessages = messageService.findBySenderId(userId);
        List<MessageResponse> inboxMessages = messageService.findByReceiverId(userId);

        List<FinancialTransactionResponse> transactions = company != null
                ? financialTransactionService.findByCompany(company.id())
                : List.of();
        List<DiscussionResponse> discussions = company != null
                ? discussionService.findByCompany(userId)
                : List.of();

        List<StudentResponse> applicants = company != null
                ? resolveApplicants(applications)
                : List.of();
        List<ResumeResponse> resumes = company != null
                ? resolveResumes(applications)
                : List.of();

        return new CompanyDashboardResponse(
                user,
                company,
                List.copyOf(jobs),
                List.copyOf(applications),
                List.copyOf(sentMessages),
                List.copyOf(inboxMessages),
                List.copyOf(transactions),
                List.copyOf(discussions),
                List.copyOf(applicants),
                List.copyOf(resumes));
    }

    public AdminDashboardResponse loadAdminDashboard(Long userId) {
        UserResponse user = userService.findById(userId);
        ensureRole(user, UserRole.ADMIN);

        AdminResponse profile = adminService.findByUserId(userId).orElse(null);

        List<UserResponse> users = userService.findAll();
        List<AdminResponse> admins = adminService.findAll();
        List<CompanyResponse> companies = companyService.findAll();
        List<StudentResponse> students = studentService.findAll();
        List<JobResponse> jobs = jobService.findAll();
        List<ResumeResponse> resumes = resumeService.findAll();
        List<ApplicationResponse> applications = applicationService.findAll();
        List<MessageResponse> inboxMessages = messageService.findByReceiverId(userId);
        List<MessageResponse> sentMessages = messageService.findBySenderId(userId);
        List<MessageResponse> allMessages = messageService.findAll();

        List<FinancialTransactionResponse> transactions = financialTransactionService.findAll();
        List<BackupResponse> backups = dataBackupService.findAll();
        List<DiscussionResponse> discussions = discussionService.findPending();

        List<AnnouncementResponse> announcements = profile != null
                ? announcementService.findByAdminId(profile.id())
                : List.of();
        List<AuditLogResponse> auditLogs = profile != null
                ? auditLogService.findByAdminId(profile.id())
                : List.of();

        return new AdminDashboardResponse(
                user,
                profile,
                List.copyOf(users),
                List.copyOf(admins),
                List.copyOf(companies),
                List.copyOf(students),
                List.copyOf(jobs),
                List.copyOf(resumes),
                List.copyOf(applications),
                List.copyOf(announcements),
                List.copyOf(auditLogs),
                List.copyOf(inboxMessages),
                List.copyOf(sentMessages),
                List.copyOf(allMessages),
                List.copyOf(transactions),
                List.copyOf(backups),
                List.copyOf(discussions));
    }

    private List<StudentResponse> resolveApplicants(List<ApplicationResponse> applications) {
        Map<Long, StudentResponse> students = new LinkedHashMap<>();
        for (ApplicationResponse application : applications) {
            try {
                StudentResponse student = studentService.findById(application.studentId());
                students.putIfAbsent(student.id(), student);
            } catch (ResourceNotFoundException ignored) {
                // 相关学生信息可能已被删除，忽略该记录
            }
        }
        return new ArrayList<>(students.values());
    }

    private List<ResumeResponse> resolveResumes(List<ApplicationResponse> applications) {
        Map<Long, ResumeResponse> resumes = new LinkedHashMap<>();
        for (ApplicationResponse application : applications) {
            try {
                ResumeResponse resume = resumeService.findById(application.resumeId());
                resumes.putIfAbsent(resume.id(), resume);
            } catch (ResourceNotFoundException ignored) {
                // 简历已被删除时忽略即可
            }
        }
        return new ArrayList<>(resumes.values());
    }

    private void ensureRole(UserResponse user, UserRole expectedRole) {
        if (user.role() != expectedRole) {
            throw new IllegalArgumentException("用户角色必须为" + expectedRole.getDisplayName());
        }
    }
}
