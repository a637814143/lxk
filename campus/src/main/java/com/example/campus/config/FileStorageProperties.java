package com.example.campus.config;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.storage")
public class FileStorageProperties {

    private String root = "storage";
    private String publicPrefix = "/uploads";
    private String resumeDir = "resumes";
    private String licenseDir = "licenses";
    private String backupDir = "backups";
    private String discussionDir = "discussions";

    public Path rootPath() {
        return Paths.get(root).toAbsolutePath().normalize();
    }

    public String getPublicPrefix() {
        return publicPrefix;
    }

    public void setPublicPrefix(String publicPrefix) {
        if (StringUtils.hasText(publicPrefix)) {
            this.publicPrefix = publicPrefix.startsWith("/") ? publicPrefix : "/" + publicPrefix;
        }
    }

    public String getResumeDir() {
        return resumeDir;
    }

    public void setResumeDir(String resumeDir) {
        if (StringUtils.hasText(resumeDir)) {
            String sanitized = sanitizeSegment(resumeDir);
            if (StringUtils.hasText(sanitized)) {
                this.resumeDir = sanitized;
            }
        }
    }

    public String getLicenseDir() {
        return licenseDir;
    }

    public void setLicenseDir(String licenseDir) {
        if (StringUtils.hasText(licenseDir)) {
            String sanitized = sanitizeSegment(licenseDir);
            if (StringUtils.hasText(sanitized)) {
                this.licenseDir = sanitized;
            }
        }
    }

    public String getBackupDir() {
        return backupDir;
    }

    public void setBackupDir(String backupDir) {
        if (StringUtils.hasText(backupDir)) {
            String sanitized = sanitizeSegment(backupDir);
            if (StringUtils.hasText(sanitized)) {
                this.backupDir = sanitized;
            }
        }
    }

    public String getDiscussionDir() {
        return discussionDir;
    }

    public void setDiscussionDir(String discussionDir) {
        if (StringUtils.hasText(discussionDir)) {
            String sanitized = sanitizeSegment(discussionDir);
            if (StringUtils.hasText(sanitized)) {
                this.discussionDir = sanitized;
            }
        }
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        if (StringUtils.hasText(root)) {
            this.root = root.trim();
        }
    }

    private String sanitizeSegment(String input) {
        return input.replaceAll("[^a-zA-Z0-9-_]", "").toLowerCase();
    }
}
