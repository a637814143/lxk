package com.example.campus.dto.portal.company;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CompanyTransactionRequest(
        @NotNull(message = "使用时长不能为空")
        @Positive(message = "使用时长必须大于0")
        Integer durationQuarters,

        String type,

        String reference,

        String notes) {
}
