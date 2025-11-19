package com.example.campus.controller;

import com.example.campus.dto.discussion.DiscussionResponse;
import com.example.campus.dto.discussion.DiscussionCommentResponse;
import com.example.campus.service.DiscussionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/discussions")
@RequiredArgsConstructor
public class DiscussionController {

    private final DiscussionService discussionService;

    @GetMapping("/company/{companyId}")
    public List<DiscussionResponse> listApprovedByCompany(@PathVariable Long companyId) {
        return discussionService.findApprovedByCompany(companyId);
    }

    @GetMapping("/{postId}/comments")
    public List<DiscussionCommentResponse> listApprovedComments(@PathVariable Long postId) {
        return discussionService.findApprovedCommentsByPost(postId);
    }
}
