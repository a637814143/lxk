package com.example.campus.dto.finance;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record FinancialTransactionRequest(
        @NotNull(message = "企业ID不能为空")
        Long companyId,

        @NotNull(message = "金额不能为空")
        @Positive(message = "金额必须大于0")
        BigDecimal amount,

        String currency,

        @NotBlank(message = "交易类型不能为空")
        String type,

        String reference,

        String notes) {
}
