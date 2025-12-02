package com.example.campus.service;

import com.example.campus.dto.discussion.DiscussionCommentCreateRequest;
import com.example.campus.dto.discussion.DiscussionCommentResponse;
import com.example.campus.dto.discussion.DiscussionCreateRequest;
import com.example.campus.dto.discussion.DiscussionResponse;
import com.example.campus.dto.discussion.DiscussionReviewRequest;
import com.example.campus.entity.TsukiAdmin;
import com.example.campus.entity.TsukiCompany;
import com.example.campus.entity.TsukiDiscussionComment;
import com.example.campus.entity.TsukiDiscussionPost;
import com.example.campus.entity.TsukiUser;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiAdminRepository;
import com.example.campus.repository.TsukiCompanyRepository;
import com.example.campus.repository.TsukiDiscussionCommentRepository;
import com.example.campus.repository.TsukiDiscussionPostRepository;
import com.example.campus.repository.TsukiUserRepository;
import java.time.LocalDateTime;
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
    private final TsukiDiscussionCommentRepository commentRepository;
    private final TsukiCompanyRepository companyRepository;
    private final TsukiUserRepository userRepository;
    private final TsukiAdminRepository adminRepository;
    private final SensitiveWordFilter sensitiveWordFilter;

    /**
     * 发布讨论时使用敏感词过滤：
     * - 未命中敏感词：状态直接设为 approved，自动对外展示；
     * - 命中敏感词：状态设为 pending，并在 reviewComment 中标记“检测到敏感词，待审核”，等待管理员复核。
     */
    @Transactional
    public DiscussionResponse create(Long authorUserId, DiscussionCreateRequest request) {
        TsukiUser author = userRepository.findById(authorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户信息"));

        // 企业用户发帖：优先使用其所属企业；学生发帖：需提供 companyId
        TsukiCompany company = companyRepository.findByUser_Id(authorUserId)
                .orElseGet(() -> {
                    if (request.companyId() == null) {
                        throw new IllegalArgumentException("请选择要发布到的企业");
                    }
                    return companyRepository.findById(request.companyId())
                            .orElseThrow(() -> new ResourceNotFoundException("未找到企业信息"));
                });

        SensitiveWordFilter.Result contentResult = sensitiveWordFilter.sanitize(request.content());
        boolean flagged = contentResult.flagged();
        String status = flagged ? "pending" : "approved";
        String reviewComment = flagged ? buildViolationComment(contentResult) : null;

        TsukiDiscussionPost post = TsukiDiscussionPost.builder()
                .author(author)
                .company(company)
                .title(request.title())
                .content(contentResult.content())
                .sanitizedContent(contentResult.content())
                .status(status)
                .reviewComment(reviewComment)
                .reviewTime(flagged ? null : LocalDateTime.now())
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

    /**
     * 管理员查看帖子列表：
     * - status 为空或 all：返回全部帖子；
     * - 否则按状态精确过滤（例如 approved / pending / rejected / deleted）。
     */
    @Transactional(readOnly = true)
    public List<DiscussionResponse> findByStatusForAdmin(String status) {
        List<TsukiDiscussionPost> posts;
        if (!StringUtils.hasText(status) || "all".equalsIgnoreCase(status)) {
            posts = discussionRepository.findAll();
        } else {
            posts = discussionRepository.findByStatus(status);
        }
        return posts.stream()
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
        post.setReviewTime(LocalDateTime.now());
        return toResponse(discussionRepository.save(post));
    }

    /**
     * 用户编辑自己的帖子：
     * - 只允许作者本人操作；
     * - 再次走敏感词过滤逻辑；
     * - 命中敏感词：pending + 审核说明；未命中：直接 approved。
     */
    @Transactional
    public DiscussionResponse updatePost(Long authorUserId, Long discussionId, DiscussionCreateRequest request) {
        TsukiDiscussionPost post = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到讨论内容"));
        if (post.getAuthor() == null || !post.getAuthor().getId().equals(authorUserId)) {
            throw new IllegalArgumentException("只能修改自己的帖子");
        }

        SensitiveWordFilter.Result contentResult = sensitiveWordFilter.sanitize(request.content());
        boolean flagged = contentResult.flagged();
        String status = flagged ? "pending" : "approved";
        String reviewComment = flagged ? buildViolationComment(contentResult) : null;

        post.setTitle(request.title());
        post.setContent(contentResult.content());
        post.setSanitizedContent(contentResult.content());
        post.setStatus(status);
        post.setReviewer(null);
        post.setReviewComment(reviewComment);
        post.setReviewTime(flagged ? null : LocalDateTime.now());

        return toResponse(discussionRepository.save(post));
    }

    /**
     * 用户删除自己的帖子：采用软删除，状态标记为 deleted，内容替换为提示语。
     */
    @Transactional
    public void deletePost(Long authorUserId, Long discussionId) {
        TsukiDiscussionPost post = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到讨论内容"));
        if (post.getAuthor() == null || !post.getAuthor().getId().equals(authorUserId)) {
            throw new IllegalArgumentException("只能删除自己的帖子");
        }
        String placeholder = "[该讨论已被作者删除]";
        post.setTitle(placeholder);
        post.setContent(placeholder);
        post.setSanitizedContent(placeholder);
        post.setStatus("deleted");
        post.setReviewer(null);
        post.setReviewComment(null);
        post.setReviewTime(LocalDateTime.now());
        discussionRepository.save(post);
    }

    /**
     * 管理员删除帖子：不校验作者，直接做软删除并记录审核人。
     */
    @Transactional
    public void adminDeletePost(Long adminUserId, Long discussionId) {
        TsukiAdmin admin = adminRepository.findByUser_Id(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("当前账号不是系统管理员"));
        TsukiDiscussionPost post = discussionRepository.findById(discussionId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到讨论内容"));
        String placeholder = "[该讨论已被管理员删除]";
        post.setTitle(placeholder);
        post.setContent(placeholder);
        post.setSanitizedContent(placeholder);
        post.setStatus("deleted");
        post.setReviewer(admin);
        post.setReviewComment(null);
        post.setReviewTime(LocalDateTime.now());
        discussionRepository.save(post);
    }

    @Transactional(readOnly = true)
    public List<DiscussionResponse> findApprovedByCompany(Long companyId) {
        return discussionRepository.findByCompany_IdAndStatus(companyId, "approved").stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<DiscussionCommentResponse> findApprovedCommentsByPost(Long postId) {
        return commentRepository.findByPost_IdAndStatus(postId, "approved").stream()
                .map(this::toCommentResponse)
                .toList();
    }

    /**
     * 评论同样做敏感词过滤，逻辑与发帖一致：
     * - 未命中敏感词：直接 approved
     * - 命中敏感词：pending + “检测到敏感词，待审核”
     */
    @Transactional
    public DiscussionCommentResponse createComment(Long authorUserId, DiscussionCommentCreateRequest request) {
        TsukiUser author = userRepository.findById(authorUserId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到用户信息"));
        TsukiDiscussionPost post = discussionRepository.findById(request.postId())
                .orElseThrow(() -> new ResourceNotFoundException("未找到讨论内容"));

        SensitiveWordFilter.Result contentResult = sensitiveWordFilter.sanitize(request.content());
        boolean flagged = contentResult.flagged();
        String status = flagged ? "pending" : "approved";
        String reviewComment = flagged ? buildViolationComment(contentResult) : null;

        TsukiDiscussionComment parent = null;
        if (request.parentCommentId() != null) {
            parent = commentRepository.findById(request.parentCommentId())
                    .orElseThrow(() -> new ResourceNotFoundException("未找到父评论内容"));
            if (!parent.getPost().getId().equals(post.getId())) {
                throw new IllegalArgumentException("父评论不属于当前讨论帖");
            }
        }

        TsukiDiscussionComment comment = TsukiDiscussionComment.builder()
                .post(post)
                .author(author)
                .parent(parent)
                .content(contentResult.content())
                .sanitizedContent(contentResult.content())
                .status(status)
                .reviewComment(reviewComment)
                .reviewTime(flagged ? null : LocalDateTime.now())
                .build();

        return toCommentResponse(commentRepository.save(comment));
    }

    /**
     * 用户编辑自己的评论：
     * - 只允许作者本人操作；
     * - 再次走敏感词过滤逻辑；
     * - 命中敏感词：pending + 审核说明；未命中：直接 approved。
     */
    @Transactional
    public DiscussionCommentResponse updateComment(Long authorUserId, Long commentId,
            DiscussionCommentCreateRequest request) {
        TsukiDiscussionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到评论内容"));
        if (comment.getAuthor() == null || !comment.getAuthor().getId().equals(authorUserId)) {
            throw new IllegalArgumentException("只能修改自己的评论");
        }
        // 校验评论是否属于指定帖子（如有传入）
        if (request.postId() != null && comment.getPost() != null
                && !comment.getPost().getId().equals(request.postId())) {
            throw new IllegalArgumentException("评论不属于指定讨论");
        }

        SensitiveWordFilter.Result contentResult = sensitiveWordFilter.sanitize(request.content());
        boolean flagged = contentResult.flagged();
        String status = flagged ? "pending" : "approved";
        String reviewComment = flagged ? buildViolationComment(contentResult) : null;

        comment.setContent(contentResult.content());
        comment.setSanitizedContent(contentResult.content());
        comment.setStatus(status);
        comment.setReviewer(null);
        comment.setReviewComment(reviewComment);
        comment.setReviewTime(flagged ? null : LocalDateTime.now());

        return toCommentResponse(commentRepository.save(comment));
    }

    /**
     * 用户删除自己的评论：
     * - 采用软删除：内容替换为提示语，状态标记为 deleted；
     * - 已删除评论不再出现在公开列表（仅查询 approved 时不会返回）。
     */
    @Transactional
    public void deleteComment(Long authorUserId, Long commentId) {
        TsukiDiscussionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到评论内容"));
        if (comment.getAuthor() == null || !comment.getAuthor().getId().equals(authorUserId)) {
            throw new IllegalArgumentException("只能删除自己的评论");
        }
        comment.setContent("[该评论已被作者删除]");
        comment.setSanitizedContent("[该评论已被作者删除]");
        comment.setStatus("deleted");
        comment.setReviewer(null);
        comment.setReviewComment(null);
        comment.setReviewTime(LocalDateTime.now());
        commentRepository.save(comment);
    }

    /**
     * 管理员删除评论：不校验作者身份，直接软删除并记录审核人。
     */
    @Transactional
    public void adminDeleteComment(Long adminUserId, Long commentId) {
        TsukiAdmin admin = adminRepository.findByUser_Id(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("当前账号不是系统管理员"));
        TsukiDiscussionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到评论内容"));
        String placeholder = "[该评论已被管理员删除]";
        comment.setContent(placeholder);
        comment.setSanitizedContent(placeholder);
        comment.setStatus("deleted");
        comment.setReviewer(admin);
        comment.setReviewComment(null);
        comment.setReviewTime(LocalDateTime.now());
        commentRepository.save(comment);
    }

    @Transactional(readOnly = true)
    public List<DiscussionCommentResponse> findPendingComments() {
        return commentRepository.findByStatus("pending").stream()
                .map(this::toCommentResponse)
                .toList();
    }

    /**
     * 管理员查看评论列表：
     * - status 为空或 all：返回全部评论；
     * - 否则按状态精确过滤（例如 approved / pending / rejected / deleted）。
     */
    @Transactional(readOnly = true)
    public List<DiscussionCommentResponse> findCommentsByStatus(String status) {
        List<TsukiDiscussionComment> list;
        if (!StringUtils.hasText(status) || "all".equalsIgnoreCase(status)) {
            list = commentRepository.findAll();
        } else {
            list = commentRepository.findByStatus(status);
        }
        return list.stream()
                .map(this::toCommentResponse)
                .toList();
    }

    @Transactional
    public DiscussionCommentResponse reviewComment(Long adminUserId, Long commentId, DiscussionReviewRequest request) {
        TsukiAdmin admin = adminRepository.findByUser_Id(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("当前账号不是系统管理员"));
        TsukiDiscussionComment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ResourceNotFoundException("未找到评论内容"));
        String status = normalizeStatus(request.status());
        comment.setStatus(status);
        comment.setReviewer(admin);
        comment.setReviewComment(request.comment());
        comment.setReviewTime(LocalDateTime.now());
        return toCommentResponse(commentRepository.save(comment));
    }

    private String buildViolationComment(SensitiveWordFilter.Result result) {
        if (result == null || !result.flagged()) {
            return null;
        }
        if (result.matchedWords() != null && !result.matchedWords().isEmpty()) {
            return "检测到敏感词（" + String.join("，", result.matchedWords()) + "），言语违规，已自动屏蔽并待审核";
        }
        return "检测到敏感词，言语违规，已自动屏蔽并待审核";
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

    private DiscussionCommentResponse toCommentResponse(TsukiDiscussionComment c) {
        return new DiscussionCommentResponse(
                c.getId(),
                c.getPost() != null ? c.getPost().getId() : null,
                c.getParent() != null ? c.getParent().getId() : null,
                c.getAuthor() != null ? c.getAuthor().getId() : null,
                c.getAuthor() != null ? c.getAuthor().getUsername() : null,
                c.getContent(),
                c.getSanitizedContent(),
                c.getStatus(),
                c.getReviewComment(),
                c.getReviewer() != null ? c.getReviewer().getId() : null,
                c.getReviewer() != null && c.getReviewer().getUser() != null
                        ? c.getReviewer().getUser().getUsername() : null,
                c.getReviewTime(),
                c.getCreatedAt(),
                c.getUpdatedAt());
    }
}
