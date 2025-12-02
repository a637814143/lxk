package com.example.campus.service;

import com.example.campus.dto.backup.BackupCreateRequest;
import com.example.campus.dto.backup.BackupResponse;
import com.example.campus.entity.TsukiAdmin;
import com.example.campus.entity.TsukiBackup;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiAdminRepository;
import com.example.campus.repository.TsukiApplicationRepository;
import com.example.campus.repository.TsukiBackupRepository;
import com.example.campus.repository.TsukiCompanyRepository;
import com.example.campus.repository.TsukiJobRepository;
import com.example.campus.repository.TsukiResumeRepository;
import com.example.campus.repository.TsukiUserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

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

        String backupType = firstNonBlank(request.backupType(), request.name(), "manual");
        String note = firstNonBlank(request.message(), request.description(), null);

        Map<String, Object> payload = new HashMap<>();
        payload.put("generatedAt", LocalDateTime.now().toString());
        payload.put("backupType", backupType);
        payload.put("message", note);

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
                    StringUtils.hasText(backupType) ? backupType : "system",
                    "json");
            byte[] data = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsBytes(payload);
            Files.write(target.absolutePath(), data);
            TsukiBackup backup = TsukiBackup.builder()
                    .admin(admin)
                    .filePath(target.publicPath())
                    .backupType(backupType)
                    .status("completed")
                    .sizeBytes((long) data.length)
                    .message(note)
                    .build();
            return toResponse(backupRepository.save(backup));
        } catch (IOException ex) {
            TsukiBackup backup = TsukiBackup.builder()
                    .admin(admin)
                    .filePath(null)
                    .backupType(backupType)
                    .status("failed")
                    .message(ex.getMessage())
                    .build();
            backupRepository.save(backup);
            throw new IllegalStateException("创建数据备份失败", ex);
        }
    }

    @Transactional
    public BackupResponse restoreBackup(Long adminUserId, MultipartFile file, String backupType, String message) {
        TsukiAdmin admin = adminRepository.findByUser_Id(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("当前账号不是系统管理员"));
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("请上传需要恢复的备份文件");
        }
        String resolvedType = firstNonBlank(backupType, "restore");
        String note = firstNonBlank(message, "上传备份文件用于恢复/留档");
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        try {
            FileStorageService.StorageTarget target =
                    fileStorageService.prepare(FileStorageService.StorageArea.BACKUP, resolvedType, extension);
            try (InputStream input = file.getInputStream()) {
                Files.copy(input, target.absolutePath(), StandardCopyOption.REPLACE_EXISTING);
            }
            TsukiBackup backup = TsukiBackup.builder()
                    .admin(admin)
                    .filePath(target.publicPath())
                    .backupType(resolvedType)
                    .status("restored")
                    .sizeBytes(file.getSize())
                    .message(note)
                    .build();
            return toResponse(backupRepository.save(backup));
        } catch (IOException ex) {
            throw new IllegalStateException("恢复/上传备份失败: " + ex.getMessage(), ex);
        }
    }

    @Transactional
    public void deleteBackup(Long adminUserId, Long backupId) {
        TsukiAdmin admin = adminRepository.findByUser_Id(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("当前账号不是系统管理员"));
        TsukiBackup backup = backupRepository.findById(backupId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到备份记录"));
        fileStorageService.deleteIfExists(backup.getFilePath());
        backupRepository.delete(backup);
    }

    private String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String v : values) {
            if (StringUtils.hasText(v)) {
                return v;
            }
        }
        return null;
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
