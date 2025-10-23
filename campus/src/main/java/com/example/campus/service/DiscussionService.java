package com.example.campus.service;

import com.example.campus.dto.discussion.DiscussionCreateRequest;
import com.example.campus.dto.discussion.DiscussionResponse;
import com.example.campus.dto.discussion.DiscussionReviewRequest;
import com.example.campus.entity.TsukiAdmin;
import com.example.campus.entity.TsukiCompany;
import com.example.campus.entity.TsukiDiscussionPost;
import com.example.campus.entity.TsukiUser;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiAdminRepository;
import com.example.campus.repository.TsukiCompanyRepository;
import com.example.campus.repository.TsukiDiscussionPostRepository;
import com.example.campus.repository.TsukiUserRepository;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class DiscussionService {

    private static final Set<String> STATUSES = Set.of("pending", "approved", "rejected");

    private final TsukiDiscussionPostRepository discussionRepository;
    private final TsukiCompanyRepository companyRepository;
    private final TsukiUserRepository userRepository;
    private final TsukiAdminRepository adminRepository;
    private final SensitiveWordFilter sensitiveWordFilter;

    @Transactional
    public DiscussionResponse create(Long authorUserId, DiscussionCreateRequest request) {
        TsukiUser author = userRepository.findById(authorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户信息"));
        TsukiCompany company = companyRepository.findByUser_Id(authorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("请先完善企业资料"));
        SensitiveWordFilter.Result result = sensitiveWordFilter.sanitize(request.content());
        TsukiDiscussionPost post = TsukiDiscussionPost.builder()
                .author(author)
                .company(company)
                .title(request.title())
                .content(request.content())
                .sanitizedContent(result.content())
                .status("pending")
                .reviewComment(result.flagged() ? "检测到敏感词，待审核" : null)
                .build();
        return toResponse(discussionRepository.save(post));
    }

    @Transactional(readOnly = true)
    public List<DiscussionResponse> findByCompany(Long companyUserId) {
        TsukiCompany company = companyRepository.findByUser_Id(companyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("请先完善企业资料"));
        return discussionRepository.findByCompany_Id(company.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DiscussionResponse> findPending() {
        return discussionRepository.findByStatus("pending").stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DiscussionResponse review(Long adminUserId, Long discussionId, DiscussionReviewRequest request) {
        TsukiAdmin admin = adminRepository.findByUser_Id(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("当前账号不是系统管理员"));
        TsukiDiscussionPost post = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到讨论内容"));
        String status = normalizeStatus(request.status());
        post.setStatus(status);
        post.setReviewer(admin);
        post.setReviewComment(request.comment());
        post.setReviewTime(java.time.LocalDateTime.now());
        return toResponse(discussionRepository.save(post));
    }

    @Transactional(readOnly = true)
    public List<DiscussionResponse> findApprovedByCompany(Long companyId) {
        return discussionRepository.findByCompany_IdAndStatus(companyId, "approved").stream()
                .map(this::toResponse)
                .toList();
    }

    private String normalizeStatus(String raw) {
        if (!StringUtils.hasText(raw)) {
            throw new IllegalArgumentException("状态不能为空");
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        if (!STATUSES.contains(normalized)) {
            throw new IllegalArgumentException("状态仅支持: " + String.join("/", STATUSES));
        }
        return normalized;
    }

    private DiscussionResponse toResponse(TsukiDiscussionPost post) {
        return new DiscussionResponse(
                post.getId(),
                post.getCompany() != null ? post.getCompany().getId() : null,
                post.getCompany() != null ? post.getCompany().getCompanyName() : null,
                post.getAuthor() != null ? post.getAuthor().getId() : null,
                post.getAuthor() != null ? post.getAuthor().getUsername() : null,
                post.getTitle(),
                post.getContent(),
                post.getSanitizedContent(),
                post.getStatus(),
                post.getReviewComment(),
                post.getReviewer() != null ? post.getReviewer().getId() : null,
                post.getReviewer() != null && post.getReviewer().getUser() != null
                        ? post.getReviewer().getUser().getUsername() : null,
                post.getReviewTime(),
                post.getCreatedAt(),
                post.getUpdatedAt());
    }
}
