package com.example.campus.dto.finance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record FinancialTransactionRequest(
        @NotNull(message = "企业ID不能为空")
        Long companyId,

        @NotNull(message = "使用时长不能为空")
        @Positive(message = "使用时长必须大于0")
        Integer durationQuarters,

        String type,

        String reference,

        String notes) {
}
