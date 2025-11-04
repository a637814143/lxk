package com.example.campus.dto.portal.company;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record CompanySubscriptionRequest(
        @Min(value = 1, message = "至少选择一个季度")
        int quarters,

        BigDecimal quarterPrice,

        @Size(max = 255, message = "备注不能超过255个字符")
        String note,

        @Size(max = 100, message = "参考编号不能超过100个字符")
        String reference) {
}
