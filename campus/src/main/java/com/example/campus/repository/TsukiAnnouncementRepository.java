package com.example.campus.repository;

import com.example.campus.entity.TsukiAnnouncement;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiAnnouncementRepository extends JpaRepository<TsukiAnnouncement, Long> {

    List<TsukiAnnouncement> findByAdmin_Id(Long adminId);

    List<TsukiAnnouncement> findByTargetInOrderByPublishTimeDesc(Collection<String> targets);
}
