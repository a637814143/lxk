package com.example.campus.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminCreateRequest(
        @NotNull(message = "关联的用户ID不能为空")
        Long userId,

        @NotBlank(message = "姓名不能为空")
        @Size(max = 50, message = "姓名不能超过50个字符")
        String name,

        Integer level) {
}
