package com.example.campus.entity;

public enum UserRole {
    ADMIN("admin", "系统管理员", "/admin/overview"),
    COMPANY("company", "企业", "/company/profile"),
    STUDENT("student", "学生", "/student/home");

    private final String dbValue;
    private final String displayName;
    private final String redirectPath;

    UserRole(String dbValue, String displayName, String redirectPath) {
        this.dbValue = dbValue;
        this.displayName = displayName;
        this.redirectPath = redirectPath;
    }

    public String getDbValue() {
        return dbValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getRedirectPath() {
        return redirectPath;
    }

    public static UserRole fromDbValue(String dbValue) {
        for (UserRole role : values()) {
            if (role.dbValue.equalsIgnoreCase(dbValue)) {
                return role;
            }
        }
        throw new IllegalArgumentException("未知的用户角色: " + dbValue);
    }
}
