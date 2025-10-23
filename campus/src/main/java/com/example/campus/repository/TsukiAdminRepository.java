package com.example.campus.repository;

import com.example.campus.entity.TsukiAdmin;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiAdminRepository extends JpaRepository<TsukiAdmin, Long> {

    Optional<TsukiAdmin> findByUser_Id(Long userId);
}
