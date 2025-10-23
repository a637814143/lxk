package com.example.campus.service;

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

    private final TsukiFinancialTransactionRepository transactionRepository;
    private final TsukiCompanyRepository companyRepository;
    private final TsukiAdminRepository adminRepository;

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
        TsukiFinancialTransaction transaction = TsukiFinancialTransaction.builder()
                .admin(admin)
                .company(company)
                .amount(request.amount())
                .currency(resolveCurrency(request.currency()))
                .type(request.type())
                .status("pending")
                .reference(request.reference())
                .notes(request.notes())
                .build();
        return toResponse(transactionRepository.save(transaction));
    }

    @Transactional
    public FinancialTransactionResponse submitByCompany(Long companyUserId, CompanyTransactionRequest request) {
        TsukiCompany company = companyRepository.findByUser_Id(companyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("请先完善企业资料"));
        TsukiFinancialTransaction transaction = TsukiFinancialTransaction.builder()
                .company(company)
                .amount(request.amount())
                .currency(resolveCurrency(request.currency()))
                .type(request.type())
                .status("pending")
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

    private String resolveCurrency(String currency) {
        if (!StringUtils.hasText(currency)) {
            return "CNY";
        }
        return currency.trim().toUpperCase(Locale.ROOT);
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
                transaction.getType(),
                transaction.getStatus(),
                transaction.getReference(),
                transaction.getNotes(),
                transaction.getCreatedAt(),
                transaction.getUpdatedAt());
    }
}
