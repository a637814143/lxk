package com.example.campus.repository;

import com.example.campus.entity.TsukiCompanyInvite;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiCompanyInviteRepository extends JpaRepository<TsukiCompanyInvite, Long> {

    Optional<TsukiCompanyInvite> findByCode(String code);

    Optional<TsukiCompanyInvite> findByCodeIgnoreCase(String code);

    boolean existsByCompanyNameHintAndStatus(String companyNameHint, String status);
}
