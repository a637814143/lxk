package com.example.campus.repository;

import com.example.campus.entity.TsukiWallet;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiWalletRepository extends JpaRepository<TsukiWallet, Long> {

    Optional<TsukiWallet> findByOwnerIdAndOwnerType(Long ownerId, String ownerType);
}
