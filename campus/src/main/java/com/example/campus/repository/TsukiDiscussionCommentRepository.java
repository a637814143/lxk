package com.example.campus.repository;

import com.example.campus.entity.TsukiDiscussionComment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiDiscussionCommentRepository extends JpaRepository<TsukiDiscussionComment, Long> {
    List<TsukiDiscussionComment> findByPost_Id(Long postId);
    List<TsukiDiscussionComment> findByPost_IdAndStatus(Long postId, String status);
    List<TsukiDiscussionComment> findByStatus(String status);
    long countByStatus(String status);
}
