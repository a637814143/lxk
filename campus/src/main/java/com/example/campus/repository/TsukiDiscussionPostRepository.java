package com.example.campus.repository;

import com.example.campus.entity.TsukiDiscussionPost;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiDiscussionPostRepository extends JpaRepository<TsukiDiscussionPost, Long> {

    List<TsukiDiscussionPost> findByCompany_Id(Long companyId);

    List<TsukiDiscussionPost> findByCompany_IdOrderByCreatedAtAsc(Long companyId);

    List<TsukiDiscussionPost> findByStatus(String status);

    List<TsukiDiscussionPost> findByCompany_IdAndStatus(Long companyId, String status);

    List<TsukiDiscussionPost> findByCompany_IdAndStatusOrderByCreatedAtAsc(Long companyId, String status);
}
