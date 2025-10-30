package com.example.campus.dto.finance;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record FinancialTransactionResponse(
        Long id,
        Long companyId,
        String companyName,
        Long adminId,
        String adminName,
        BigDecimal amount,
        String currency,
        Integer durationMonths,
        String type,
        String status,
        String reference,
        String notes,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {
}
