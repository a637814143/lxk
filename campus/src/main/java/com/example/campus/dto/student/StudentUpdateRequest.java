package com.example.campus.dto.student;

import jakarta.validation.constraints.Size;

public record StudentUpdateRequest(
        @Size(max = 50, message = "姓名不能超过50个字符")
        String name,

        @Size(max = 5, message = "性别不能超过5个字符")
        String gender,

        @Size(max = 100, message = "学校名称不能超过100个字符")
        String school,

        @Size(max = 100, message = "专业不能超过100个字符")
        String major,

        @Size(max = 20, message = "年级不能超过20个字符")
        String grade,

        @Size(max = 50, message = "学历不能超过50个字符")
        String education,

        @Size(max = 255, message = "头像链接不能超过255个字符")
        String avatar) {
}
