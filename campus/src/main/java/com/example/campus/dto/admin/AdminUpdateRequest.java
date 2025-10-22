package com.example.campus.dto.admin;

import jakarta.validation.constraints.Size;

public record AdminUpdateRequest(
        @Size(max = 50, message = "姓名不能超过50个字符")
        String name,

        Integer level) {
}
