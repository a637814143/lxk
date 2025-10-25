package com.example.campus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
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
@Table(name = "tsuki_company_invite")
public class TsukiCompanyInvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "invite_id")
    private Long id;

    @Column(name = "code", nullable = false, length = 64, unique = true)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private TsukiAdmin admin;

    @Column(name = "note", length = 255)
    private String note;

    @Column(name = "company_name_hint", length = 100)
    private String companyNameHint;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @Column(name = "created_time", insertable = false, updatable = false)
    private LocalDateTime createdTime;

    @Column(name = "used_time")
    private LocalDateTime usedTime;

    @PrePersist
    public void prePersist() {
        if (status == null || status.isBlank()) {
            status = "active";
        }
    }
}
