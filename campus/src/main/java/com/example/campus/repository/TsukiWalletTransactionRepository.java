package com.example.campus.repository;

import com.example.campus.entity.TsukiWalletTransaction;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiWalletTransactionRepository extends JpaRepository<TsukiWalletTransaction, Long> {

    List<TsukiWalletTransaction> findByWallet_Id(Long walletId);
}
