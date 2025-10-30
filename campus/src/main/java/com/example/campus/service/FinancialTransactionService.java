package com.example.campus.service;

import com.example.campus.config.BillingProperties;
import com.example.campus.dto.finance.FinancialTransactionRequest;
import com.example.campus.dto.finance.FinancialTransactionResponse;
import com.example.campus.dto.finance.FinancialTransactionStatusRequest;
import com.example.campus.dto.portal.company.CompanyTransactionRequest;
import com.example.campus.entity.TsukiAdmin;
import com.example.campus.entity.TsukiCompany;
import com.example.campus.entity.TsukiFinancialTransaction;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiAdminRepository;
import com.example.campus.repository.TsukiCompanyRepository;
import com.example.campus.repository.TsukiFinancialTransactionRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class FinancialTransactionService {

    private static final Set<String> STATUSES = Set.of("pending", "completed", "cancelled");
    private static final String BILLING_CURRENCY = "CNY";
    private static final int MONTHS_PER_QUARTER = 3;

    private final TsukiFinancialTransactionRepository transactionRepository;
    private final TsukiCompanyRepository companyRepository;
    private final TsukiAdminRepository adminRepository;
    private final BillingProperties billingProperties;

    @Transactional(readOnly = true)
    public List<FinancialTransactionResponse> findAll() {
        return transactionRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FinancialTransactionResponse> findByCompany(Long companyId) {
        return transactionRepository.findByCompany_Id(companyId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public FinancialTransactionResponse createByAdmin(Long adminUserId, FinancialTransactionRequest request) {
        TsukiAdmin admin = requireAdmin(adminUserId);
        TsukiCompany company = companyRepository.findById(request.companyId())
                .orElseThrow(() -> new ResourceNotFoundException("未找到企业信息"));
        int durationQuarters = normalizeDuration(request.durationQuarters());
        BigDecimal amount = calculateAmount(durationQuarters);
        deductFromWallet(company, amount);
        TsukiFinancialTransaction transaction = TsukiFinancialTransaction.builder()
                .admin(admin)
                .company(company)
                .amount(amount)
                .currency(BILLING_CURRENCY)
                .durationMonths(durationQuarters * MONTHS_PER_QUARTER)
                .type(resolveType(request.type()))
                .status("completed")
                .reference(request.reference())
                .notes(request.notes())
                .build();
        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public FinancialTransactionResponse submitByCompany(Long companyUserId, CompanyTransactionRequest request) {
        TsukiCompany company = companyRepository.findByUser_Id(companyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("请先完善企业资料"));
        int durationQuarters = normalizeDuration(request.durationQuarters());
        BigDecimal amount = calculateAmount(durationQuarters);
        deductFromWallet(company, amount);
        TsukiFinancialTransaction transaction = TsukiFinancialTransaction.builder()
                .company(company)
                .amount(amount)
                .currency(BILLING_CURRENCY)
                .durationMonths(durationQuarters * MONTHS_PER_QUARTER)
                .type(resolveType(request.type()))
                .status("completed")
                .reference(request.reference())
                .notes(request.notes())
                .build();
        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public FinancialTransactionResponse updateStatus(Long adminUserId, Long transactionId,
            FinancialTransactionStatusRequest request) {
        TsukiAdmin admin = requireAdmin(adminUserId);
        TsukiFinancialTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到交易记录"));
        String status = normalizeStatus(request.status());
        transaction.setStatus(status);
        transaction.setNotes(request.notes());
        if (transaction.getAdmin() == null) {
            transaction.setAdmin(admin);
        }
        return toResponse(transactionRepository.save(transaction));
    }

    private String normalizeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("状态不能为空");
        }
        String normalized = status.trim().toLowerCase(Locale.ROOT);
        if (!STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("状态仅支持: " + String.join("/", STATUSES));
        }
        return normalized;
    }

    private TsukiAdmin requireAdmin(Long userId) {
        return adminRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("当前账号不是系统管理员"));
    }

    private FinancialTransactionResponse toResponse(TsukiFinancialTransaction transaction) {
        return new FinancialTransactionResponse(
                transaction.getId(),
                transaction.getCompany() != null ? transaction.getCompany().getId() : null,
                transaction.getCompany() != null ? transaction.getCompany().getCompanyName() : null,
                transaction.getAdmin() != null ? transaction.getAdmin().getId() : null,
                transaction.getAdmin() != null && transaction.getAdmin().getUser() != null
                        ? transaction.getAdmin().getUser().getUsername() : null,
                transaction.getAmount(),
                transaction.getCurrency(),
                transaction.getDurationMonths(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getReference(),
                transaction.getNotes(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt());
    }

    private BigDecimal calculateAmount(int durationQuarters) {
        if (durationQuarters <= 0) {
            throw new IllegalArgumentException("使用时长必须大于0");
        }
        BigDecimal quarterFee = billingProperties.getQuarterFee();
        return quarterFee.multiply(BigDecimal.valueOf(durationQuarters));
    }

    private int normalizeDuration(Integer durationQuarters) {
        if (durationQuarters == null || durationQuarters <= 0) {
            throw new IllegalArgumentException("使用时长必须大于0");
        }
        return durationQuarters;
    }

    private void deductFromWallet(TsukiCompany company, BigDecimal amount) {
        BigDecimal current = company.getWalletBalance() == null ? BigDecimal.ZERO : company.getWalletBalance();
        if (current.compareTo(amount) < 0) {
            throw new IllegalArgumentException("企业钱包余额不足，无法完成扣款");
        }
        company.setWalletBalance(current.subtract(amount));
    }

    private String resolveType(String type) {
        if (!StringUtils.hasText(type)) {
            return "季度服务费";
        }
        return type.trim();
    }
}
