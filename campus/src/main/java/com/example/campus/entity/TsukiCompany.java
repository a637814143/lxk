package com.example.campus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "tsuki_company")
public class TsukiCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private TsukiUser user;

    @Column(name = "company_name", nullable = false, length = 100)
    private String companyName;

    @Column(name = "license_number", length = 100)
    private String licenseNumber;

    @Column(name = "industry", length = 100)
    private String industry;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "website", length = 255)
    private String website;

    @Column(name = "description")
    private String description;

    @Column(name = "logo", length = 255)
    private String logo;

    @Column(name = "license_document", length = 255)
    private String licenseDocument;

    @Column(name = "audit_status", length = 20)
    private String auditStatus;

    @Column(name = "audit_reason", length = 255)
    private String auditReason;

    @Column(name = "wallet_balance", precision = 12, scale = 2, nullable = false)
    private BigDecimal walletBalance;

    @PrePersist
    public void initWallet() {
        if (walletBalance == null) {
            walletBalance = BigDecimal.ZERO;
        }
    }
}
