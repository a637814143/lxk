package com.example.campus.repository;

import com.example.campus.entity.TsukiCompanyInvite;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface TsukiCompanyInviteRepository extends JpaRepository<TsukiCompanyInvite, Long> {

    Optional<TsukiCompanyInvite> findByCodeIgnoreCase(String code);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    Optional<TsukiCompanyInvite> findWithLockByCodeIgnoreCase(String code);

    long countByStatus(String status);

    List<TsukiCompanyInvite> findAllByOrderByCreatedTimeDesc();
}
