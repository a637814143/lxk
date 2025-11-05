package com.example.campus.service;

import com.example.campus.dto.discussion.DiscussionCreateRequest;
import com.example.campus.dto.discussion.DiscussionResponse;
import com.example.campus.dto.discussion.DiscussionReviewRequest;
import com.example.campus.entity.TsukiAdmin;
import com.example.campus.entity.TsukiCompany;
import com.example.campus.entity.TsukiDiscussionPost;
import com.example.campus.entity.TsukiUser;
import com.example.campus.entity.UserRole;
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
        TsukiCompany company = resolveCompany(author, request.companyId());
        TsukiDiscussionPost parent = resolveParent(request.parentId(), company);
        SensitiveWordFilter.Result result = sensitiveWordFilter.sanitize(request.content());
        String title = determineTitle(request.title(), result.content(), parent, author.getUsername());
        TsukiDiscussionPost post = TsukiDiscussionPost.builder()
                .author(author)
                .company(company)
                .parent(parent)
                .title(title)
                .content(request.content())
                .sanitizedContent(result.content())
                .status("approved")
                .reviewComment(null)
                .build();
        return toResponse(discussionRepository.save(post));
    }

    @Transactional(readOnly = true)
    public List<DiscussionResponse> findByCompany(Long companyUserId) {
        TsukiCompany company = companyRepository.findByUser_Id(companyUserId)
                .orElseThrow(() -> new ResourceNotFoundException("请先完善企业资料"));
        return discussionRepository.findByCompany_IdOrderByCreatedAtAsc(company.getId()).stream()
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
        return discussionRepository.findByCompany_IdAndStatusOrderByCreatedAtAsc(companyId, "approved").stream()
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

    private TsukiCompany resolveCompany(TsukiUser author, Long requestedCompanyId) {
        if (author.getRole() == UserRole.COMPANY) {
            TsukiCompany company = companyRepository.findByUser_Id(author.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("请先完善企业资料"));
            if (requestedCompanyId != null && !company.getId().equals(requestedCompanyId)) {
                throw new IllegalArgumentException("不能为其他企业创建讨论");
            }
            return company;
        }
        if (requestedCompanyId == null) {
            throw new IllegalArgumentException("请选择要讨论的企业");
        }
        return companyRepository.findById(requestedCompanyId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到企业信息"));
    }

    private TsukiDiscussionPost resolveParent(Long parentId, TsukiCompany company) {
        if (parentId == null) {
            return null;
        }
        TsukiDiscussionPost parent = discussionRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到评论内容"));
        if (!parent.getCompany().getId().equals(company.getId())) {
            throw new IllegalArgumentException("回复的评论不属于当前企业");
        }
        return parent;
    }

    private String determineTitle(String rawTitle, String sanitizedContent, TsukiDiscussionPost parent, String authorName) {
        String title = StringUtils.hasText(rawTitle) ? rawTitle.trim() : null;
        if (!StringUtils.hasText(title)) {
            if (parent != null) {
                String parentAuthor = parent.getAuthor() != null ? parent.getAuthor().getUsername() : "评论";
                title = "回复 " + parentAuthor;
            } else {
                String base = StringUtils.hasText(sanitizedContent) ? sanitizedContent : "来自" + authorName + "的评论";
                title = base.length() > 20 ? base.substring(0, 20) : base;
            }
        }
        return title.length() > 100 ? title.substring(0, 100) : title;
    }

    private DiscussionResponse toResponse(TsukiDiscussionPost post) {
        return new DiscussionResponse(
                post.getId(),
                post.getCompany() != null ? post.getCompany().getId() : null,
                post.getCompany() != null ? post.getCompany().getCompanyName() : null,
                post.getAuthor() != null ? post.getAuthor().getId() : null,
                post.getAuthor() != null ? post.getAuthor().getUsername() : null,
                post.getAuthor() != null && post.getAuthor().getRole() != null
                        ? post.getAuthor().getRole().getDisplayName()
                        : null,
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
                post.getUpdatedAt(),
                post.getParent() != null ? post.getParent().getId() : null);
    }
}
