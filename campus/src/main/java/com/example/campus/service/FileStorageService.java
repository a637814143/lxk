package com.example.campus.service;

import com.example.campus.config.FileStorageProperties;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private final FileStorageProperties properties;

    public FileStorageService(FileStorageProperties properties) {
        this.properties = properties;
    }

    @PostConstruct
    public void prepareDirectories() {
        try {
            Files.createDirectories(resolve(StorageArea.RESUME));
            Files.createDirectories(resolve(StorageArea.LICENSE));
            Files.createDirectories(resolve(StorageArea.BACKUP));
            Files.createDirectories(resolve(StorageArea.DISCUSSION));
        } catch (IOException ex) {
            throw new IllegalStateException("无法初始化文件存储目录", ex);
        }
    }

    public String store(MultipartFile file, StorageArea area, String prefix) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "file" : file.getOriginalFilename());
        String extension = StringUtils.getFilenameExtension(originalName);
        StorageTarget target = prepare(area, prefix, extension);
        try (InputStream input = file.getInputStream()) {
            Files.copy(input, target.absolutePath(), StandardCopyOption.REPLACE_EXISTING);
        }
        return target.publicPath();
    }

    public StorageTarget prepare(StorageArea area, String prefix, String extension) throws IOException {
        Path directory = resolve(area);
        Files.createDirectories(directory);
        String safePrefix = StringUtils.hasText(prefix) ? prefix.replaceAll("[^a-zA-Z0-9-_]", "").toLowerCase(Locale.ROOT) : area.name().toLowerCase(Locale.ROOT);
        StringBuilder filename = new StringBuilder();
        filename.append(safePrefix);
        filename.append("-").append(FORMATTER.format(LocalDateTime.now()));
        filename.append("-").append(UUID.randomUUID().toString().replaceAll("-", ""));
        if (StringUtils.hasText(extension)) {
            filename.append('.').append(extension.toLowerCase(Locale.ROOT));
        }
        Path absolute = directory.resolve(filename.toString()).normalize();
        return new StorageTarget(absolute, buildPublicPath(area, filename.toString()));
    }

    public void deleteIfExists(String publicPath) {
        if (!StringUtils.hasText(publicPath)) {
            return;
        }
        String prefix = properties.getPublicPrefix();
        String relative = publicPath;
        if (StringUtils.hasText(prefix) && relative.startsWith(prefix)) {
            relative = relative.substring(prefix.length());
        }
        relative = relative.replaceFirst("^/", "");
        Path file = properties.rootPath().resolve(relative);
        try {
            Files.deleteIfExists(file);
        } catch (IOException ignored) {
            // 删除失败不影响主流程
        }
    }

    public Path resolve(StorageArea area) {
        return switch (area) {
            case RESUME -> properties.rootPath().resolve(properties.getResumeDir());
            case LICENSE -> properties.rootPath().resolve(properties.getLicenseDir());
            case BACKUP -> properties.rootPath().resolve(properties.getBackupDir());
            case DISCUSSION -> properties.rootPath().resolve(properties.getDiscussionDir());
        };
    }

    public String buildPublicPath(StorageArea area, String filename) {
        String directory = switch (area) {
            case RESUME -> properties.getResumeDir();
            case LICENSE -> properties.getLicenseDir();
            case BACKUP -> properties.getBackupDir();
            case DISCUSSION -> properties.getDiscussionDir();
        };
        return properties.getPublicPrefix() + "/" + directory + "/" + filename;
    }

    public record StorageTarget(Path absolutePath, String publicPath) {
    }

    public enum StorageArea {
        RESUME,
        LICENSE,
        BACKUP,
        DISCUSSION
    }
}
