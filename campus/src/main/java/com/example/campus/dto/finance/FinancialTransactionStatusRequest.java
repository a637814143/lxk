package com.example.campus.dto.finance;

import jakarta.validation.constraints.NotBlank;

public record FinancialTransactionStatusRequest(
        @NotBlank(message = "状态不能为空")
        String status,

        String notes) {
}
