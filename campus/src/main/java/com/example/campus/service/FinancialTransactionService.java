package com.example.campus.service;

import com.example.campus.dto.finance.FinancialTransactionRequest;
import com.example.campus.dto.finance.FinancialTransactionResponse;
import com.example.campus.dto.finance.FinancialTransactionStatusRequest;
import com.example.campus.dto.portal.company.CompanySubscriptionRequest;
import com.example.campus.dto.portal.company.CompanyTransactionRequest;
import com.example.campus.dto.portal.company.CompanyRechargeRequest;
import com.example.campus.entity.TsukiAdmin;
import com.example.campus.entity.TsukiCompany;
import com.example.campus.entity.TsukiFinancialTransaction;
import com.example.campus.entity.TsukiWallet;
import com.example.campus.entity.TsukiWalletTransaction;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiAdminRepository;
import com.example.campus.repository.TsukiCompanyRepository;
import com.example.campus.repository.TsukiFinancialTransactionRepository;
import com.example.campus.repository.TsukiWalletRepository;
import com.example.campus.repository.TsukiWalletTransactionRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private static final Set<String> WALLET_TYPES = Set.of("recharge", "withdraw", "transfer", "payment", "refund");
    private static final BigDecimal DEFAULT_QUARTER_PRICE = new BigDecimal("1999.00");

    private final TsukiFinancialTransactionRepository transactionRepository;
    private final TsukiCompanyRepository companyRepository;
    private final TsukiAdminRepository adminRepository;
    private final TsukiWalletRepository walletRepository;
    private final TsukiWalletTransactionRepository walletTransactionRepository;

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
    public FinancialTransactionResponse purchaseSubscription(Long companyUserId, CompanySubscriptionRequest request) {
        TsukiCompany company = companyRepository.findByUser_Id(companyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("请先完善企业资料"));
        int quarters = request.quarters();
        if (quarters <= 0) {
            throw new IllegalArgumentException("至少选择一个季度");
        }
        BigDecimal quarterPrice = request.quarterPrice();
        if (quarterPrice == null || quarterPrice.compareTo(BigDecimal.ZERO) <= 0) {
            quarterPrice = DEFAULT_QUARTER_PRICE;
        }
        quarterPrice = quarterPrice.setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = quarterPrice.multiply(BigDecimal.valueOf(quarters)).setScale(2, RoundingMode.HALF_UP);

        TsukiWallet wallet = walletRepository.findByOwnerIdAndOwnerType(company.getId(), "company")
                .orElseGet(() -> walletRepository.save(TsukiWallet.builder()
                        .ownerId(company.getId())
                        .ownerType("company")
                        .balance(BigDecimal.ZERO)
                        .build()));
        if (wallet.getBalance().compareTo(totalAmount) < 0) {
            throw new IllegalArgumentException("企业钱包余额不足，请先充值");
        }

        TsukiAdmin admin = adminRepository.findFirstByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("系统尚未配置管理员账户，无法完成扣款"));
        String notes = buildSubscriptionNote(quarters, quarterPrice, request.note());

        TsukiFinancialTransaction transaction = TsukiFinancialTransaction.builder()
                .company(company)
                .admin(admin)
                .amount(totalAmount)
                .currency("CNY")
                .type("subscription")
                .status("completed")
                .reference(request.reference())
                .notes(notes)
                .build();
        transaction = transactionRepository.save(transaction);
        applyWalletAdjustment(transaction);
        return toResponse(transaction);
    }

    /**
     * 企业虚拟充值：直接将指定金额划入企业钱包，用于测试/演示。
     */
    @Transactional
    public FinancialTransactionResponse rechargeByCompany(Long companyUserId, CompanyRechargeRequest request) {
        TsukiCompany company = companyRepository.findByUser_Id(companyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("请先完善企业资料"));
        BigDecimal amount = request.amount();
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("充值金额必须大于0");
        }
        TsukiFinancialTransaction transaction = TsukiFinancialTransaction.builder()
                .company(company)
                .amount(amount.setScale(2, RoundingMode.HALF_UP))
                .currency(resolveCurrency(request.currency()))
                .type("recharge")
                .status("completed")
                .reference(request.reference())
                .notes(request.note())
                .build();
        transaction = transactionRepository.save(transaction);
        applyWalletAdjustment(transaction);
        return toResponse(transaction);
    }

    @Transactional
    public FinancialTransactionResponse updateStatus(Long adminUserId, Long transactionId,
            FinancialTransactionStatusRequest request) {
        TsukiAdmin admin = requireAdmin(adminUserId);
        TsukiFinancialTransaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到交易记录"));
        String previousStatus = transaction.getStatus();
        String status = normalizeStatus(request.status());
        transaction.setStatus(status);
        transaction.setNotes(request.notes());
        if (transaction.getAdmin() == null) {
            transaction.setAdmin(admin);
        }
        if (!"completed".equalsIgnoreCase(previousStatus) && "completed".equals(status)) {
            applyWalletAdjustment(transaction);
        }
        return toResponse(transactionRepository.save(transaction));
    }

    private String resolveCurrency(String currency) {
        if (!StringUtils.hasText(currency)) {
            return "CNY";
        }
        return currency.trim().toUpperCase(Locale.ROOT);
    }

    private void applyWalletAdjustment(TsukiFinancialTransaction transaction) {
        if (transaction.getCompany() == null || transaction.getAmount() == null) {
            return;
        }
        TsukiCompany company = transaction.getCompany();
        TsukiWallet wallet = walletRepository.findByOwnerIdAndOwnerType(company.getId(), "company")
                .orElseGet(() -> walletRepository.save(TsukiWallet.builder()
                        .ownerId(company.getId())
                        .ownerType("company")
                        .balance(BigDecimal.ZERO)
                        .build()));
        BigDecimal signedAmount = resolveSignedAmount(transaction.getType(), transaction.getAmount());
        BigDecimal updatedBalance = wallet.getBalance().add(signedAmount);
        if (signedAmount.signum() < 0 && updatedBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("企业钱包余额不足，无法完成扣款");
        }
        wallet.setBalance(updatedBalance);
        walletRepository.save(wallet);

        walletTransactionRepository.save(TsukiWalletTransaction.builder()
                .wallet(wallet)
                .amount(signedAmount)
                .type(resolveWalletTransactionType(transaction.getType(), signedAmount))
                .description(transaction.getNotes())
                .referenceId(transaction.getId())
                .build());

        company.setWalletBalance(wallet.getBalance());
        companyRepository.save(company);

        if (transaction.getAdmin() != null && transaction.getAmount() != null && signedAmount.signum() < 0) {
            TsukiAdmin admin = transaction.getAdmin();
            TsukiWallet adminWallet = walletRepository.findByOwnerIdAndOwnerType(admin.getId(), "admin")
                    .orElseGet(() -> walletRepository.save(TsukiWallet.builder()
                            .ownerId(admin.getId())
                            .ownerType("admin")
                            .balance(BigDecimal.ZERO)
                            .build()));
            adminWallet.setBalance(adminWallet.getBalance().add(transaction.getAmount()));
            walletRepository.save(adminWallet);

            walletTransactionRepository.save(TsukiWalletTransaction.builder()
                    .wallet(adminWallet)
                    .amount(transaction.getAmount())
                    .type("recharge")
                    .description(transaction.getNotes())
                    .referenceId(transaction.getId())
                    .build());
        }
    }

    private BigDecimal resolveSignedAmount(String transactionType, BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO;
        }
        if (!StringUtils.hasText(transactionType)) {
            return amount;
        }
        String normalized = transactionType.trim().toLowerCase(Locale.ROOT);
        return switch (normalized) {
            case "withdraw", "payment", "transfer", "subscription" -> amount.negate();
            default -> amount;
        };
    }

    private String resolveWalletTransactionType(String type, BigDecimal signedAmount) {
        if (StringUtils.hasText(type)) {
            String normalized = type.trim().toLowerCase(Locale.ROOT);
            if (WALLET_TYPES.contains(normalized)) {
                return normalized;
            }
        }
        return signedAmount.signum() >= 0 ? "recharge" : "payment";
    }

    private String buildSubscriptionNote(int quarters, BigDecimal quarterPrice, String note) {
        String base = "购买" + quarters + "个季度服务，每季度￥" + quarterPrice.toPlainString();
        if (StringUtils.hasText(note)) {
            return base + "；备注：" + note.trim();
        }
        return base;
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
