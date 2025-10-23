package com.example.campus.service;

import com.example.campus.dto.backup.BackupCreateRequest;
import com.example.campus.dto.backup.BackupResponse;
import com.example.campus.entity.TsukiAdmin;
import com.example.campus.entity.TsukiBackup;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiAdminRepository;
import com.example.campus.repository.TsukiBackupRepository;
import com.example.campus.repository.TsukiApplicationRepository;
import com.example.campus.repository.TsukiCompanyRepository;
import com.example.campus.repository.TsukiJobRepository;
import com.example.campus.repository.TsukiResumeRepository;
import com.example.campus.repository.TsukiUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DataBackupService {

    private final TsukiBackupRepository backupRepository;
    private final TsukiAdminRepository adminRepository;
    private final TsukiUserRepository userRepository;
    private final TsukiCompanyRepository companyRepository;
    private final TsukiJobRepository jobRepository;
    private final TsukiResumeRepository resumeRepository;
    private final TsukiApplicationRepository applicationRepository;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<BackupResponse> findAll() {
        return backupRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public BackupResponse createBackup(Long adminUserId, BackupCreateRequest request) {
        TsukiAdmin admin = adminRepository.findByUser_Id(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("当前账号不是系统管理员"));

        Map<String, Object> payload = new HashMap<>();
        payload.put("generatedAt", LocalDateTime.now().toString());
        payload.put("backupType", request.backupType());
        payload.put("message", request.message());

        Map<String, Object> statistics = new HashMap<>();
        statistics.put("totalUsers", userRepository.count());
        statistics.put("totalCompanies", companyRepository.count());
        statistics.put("totalJobs", jobRepository.count());
        statistics.put("totalResumes", resumeRepository.count());
        statistics.put("totalApplications", applicationRepository.count());
        payload.put("statistics", statistics);

        try {
            FileStorageService.StorageTarget target = fileStorageService.prepare(
                    FileStorageService.StorageArea.BACKUP,
                    StringUtils.hasText(request.backupType()) ? request.backupType() : "system",
                    "json");
            byte[] data = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(payload);
            Files.write(target.absolutePath(), data);
            TsukiBackup backup = TsukiBackup.builder()
                    .admin(admin)
                    .filePath(target.publicPath())
                    .backupType(request.backupType())
                    .status("completed")
                    .sizeBytes((long) data.length)
                    .message(request.message())
                    .build();
            return toResponse(backupRepository.save(backup));
        } catch (IOException ex) {
            TsukiBackup backup = TsukiBackup.builder()
                    .admin(admin)
                    .filePath(null)
                    .backupType(request.backupType())
                    .status("failed")
                    .message(ex.getMessage())
                    .build();
            backupRepository.save(backup);
            throw new IllegalStateException("创建数据备份失败", ex);
        }
    }

    private BackupResponse toResponse(TsukiBackup backup) {
        return new BackupResponse(
                backup.getId(),
                backup.getAdmin() != null ? backup.getAdmin().getId() : null,
                backup.getAdmin() != null && backup.getAdmin().getUser() != null
                        ? backup.getAdmin().getUser().getUsername() : null,
                backup.getFilePath(),
                backup.getFilePath(),
                backup.getBackupType(),
                backup.getStatus(),
                backup.getSizeBytes(),
                backup.getMessage(),
                backup.getCreatedAt());
    }
}
