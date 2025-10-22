package com.example.campus.repository;

import com.example.campus.entity.TsukiMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiMessageRepository extends JpaRepository<TsukiMessage, Long> {
}
