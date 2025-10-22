package com.example.campus.repository;

import com.example.campus.entity.TsukiMessage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiMessageRepository extends JpaRepository<TsukiMessage, Long> {

    List<TsukiMessage> findByReceiver_Id(Long receiverId);

    List<TsukiMessage> findBySenderId(Long senderId);
}
