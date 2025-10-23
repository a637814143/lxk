package com.example.campus.dto.portal.company;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CompanyTransactionRequest(
        @NotNull(message = "金额不能为空")
        @Positive(message = "金额必须大于0")
        BigDecimal amount,

        @NotBlank(message = "交易类型不能为空")
        String type,

        String currency,

        String reference,

        String notes) {
}
