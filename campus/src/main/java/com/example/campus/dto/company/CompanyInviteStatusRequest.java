package com.example.campus.dto.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CompanyInviteStatusRequest(
        @NotBlank(message = "状态不能为空")
        @Size(max = 20, message = "状态不能超过20个字符")
        String status) {
}
