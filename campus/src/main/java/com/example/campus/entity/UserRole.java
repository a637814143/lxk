package com.example.campus.entity;

public enum UserRole {
    SYSTEM_ADMIN("系统管理员", "/admin/dashboard"),
    COMPANY("企业", "/company/home"),
    STUDENT("学生", "/student/home");

    private final String displayName;
    private final String redirectPath;

    UserRole(String displayName, String redirectPath) {
        this.displayName = displayName;
        this.redirectPath = redirectPath;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRedirectPath() {
        return redirectPath;
    }
}
