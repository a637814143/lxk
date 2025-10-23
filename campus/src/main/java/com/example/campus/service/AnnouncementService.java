package com.example.campus.service;

import com.example.campus.dto.announcement.AnnouncementCreateRequest;
import com.example.campus.dto.announcement.AnnouncementResponse;
import com.example.campus.dto.announcement.AnnouncementUpdateRequest;
import com.example.campus.entity.TsukiAdmin;
import com.example.campus.entity.TsukiAnnouncement;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiAdminRepository;
import com.example.campus.repository.TsukiAnnouncementRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnnouncementService {

    private static final Set<String> ALLOWED_TARGETS = Set.of("all", "student", "company");

    private final TsukiAnnouncementRepository announcementRepository;
    private final TsukiAdminRepository adminRepository;

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> findAll() {
        return announcementRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> findByAdminId(Long adminId) {
        return announcementRepository.findByAdmin_Id(adminId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AnnouncementResponse> findByTargets(List<String> targets) {
        return announcementRepository.findByTargetInOrderByPublishTimeDesc(targets).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public AnnouncementResponse findById(Long id) {
        return toResponse(getAnnouncement(id));
    }

    @Transactional
    public AnnouncementResponse create(AnnouncementCreateRequest request) {
        TsukiAdmin admin = getAdmin(request.adminId());
        TsukiAnnouncement announcement = TsukiAnnouncement.builder()
                .admin(admin)
                .title(request.title())
                .content(request.content())
                .target(normalizeTarget(request.target()))
                .build();
        TsukiAnnouncement saved = announcementRepository.save(announcement);
        return toResponse(saved);
    }

    @Transactional
    public AnnouncementResponse update(Long id, AnnouncementUpdateRequest request) {
        TsukiAnnouncement announcement = getAnnouncement(id);
        if (request.title() != null) {
            announcement.setTitle(request.title());
        }
        if (request.content() != null) {
            announcement.setContent(request.content());
        }
        if (request.target() != null) {
            announcement.setTarget(normalizeTarget(request.target()));
        }
        return toResponse(announcement);
    }

    @Transactional
    public void delete(Long id) {
        TsukiAnnouncement announcement = getAnnouncement(id);
        announcementRepository.delete(announcement);
    }

    private TsukiAnnouncement getAnnouncement(Long id) {
        return announcementRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的公告"));
    }

    private TsukiAdmin getAdmin(Long id) {
        return adminRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的管理员"));
    }

    private AnnouncementResponse toResponse(TsukiAnnouncement announcement) {
        return new AnnouncementResponse(
                announcement.getId(),
                announcement.getAdmin().getId(),
                announcement.getTitle(),
                announcement.getContent(),
                announcement.getTarget(),
                announcement.getPublishTime());
    }

    private String normalizeTarget(String target) {
        if (target == null || target.isBlank()) {
            return "all";
        }
        String normalized = target.toLowerCase();
        if (!ALLOWED_TARGETS.contains(normalized)) {
            throw new IllegalArgumentException("公告目标仅支持: " + String.join("/", ALLOWED_TARGETS));
        }
        return normalized;
    }
}
