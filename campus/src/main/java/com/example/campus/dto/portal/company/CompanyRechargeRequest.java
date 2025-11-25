package com.example.campus.dto.portal.company;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/**
 * 企业虚拟充值请求：仅用于平台内测试/演示钱包充值，不做真实支付对接。
 */
public record CompanyRechargeRequest(
        @NotNull(message = "充值金额不能为空")
        @Positive(message = "充值金额必须大于0")
        BigDecimal amount,

        String currency,

        @Size(max = 100, message = "参考编号不能超出100个字符")
        String reference,

        @Size(max = 255, message = "备注不能超过255个字符")
        String note) {
}

