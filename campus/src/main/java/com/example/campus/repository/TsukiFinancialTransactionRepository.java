package com.example.campus.repository;

import com.example.campus.entity.TsukiFinancialTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiFinancialTransactionRepository extends JpaRepository<TsukiFinancialTransaction, Long> {

    List<TsukiFinancialTransaction> findByCompany_Id(Long companyId);

    List<TsukiFinancialTransaction> findByStatus(String status);

    List<TsukiFinancialTransaction> findByCompany_IdAndTypeAndStatusOrderByCreatedAtAsc(Long companyId, String type,
            String status);
}
