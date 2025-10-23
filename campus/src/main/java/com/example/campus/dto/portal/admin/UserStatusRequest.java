package com.example.campus.dto.portal.admin;

import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(
        @NotNull(message = "状态不能为空")
        Integer status) {
}
