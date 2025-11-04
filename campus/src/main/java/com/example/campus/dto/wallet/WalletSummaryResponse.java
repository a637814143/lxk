package com.example.campus.dto.wallet;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record WalletSummaryResponse(
        Long ownerId,
        String ownerType,
        BigDecimal balance,
        LocalDateTime updatedAt) {
}
