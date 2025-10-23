package com.example.campus.repository;

import com.example.campus.entity.TsukiBackup;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiBackupRepository extends JpaRepository<TsukiBackup, Long> {

    List<TsukiBackup> findByAdmin_Id(Long adminId);
}
