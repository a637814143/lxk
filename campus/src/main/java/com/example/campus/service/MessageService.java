package com.example.campus.service;

import com.example.campus.dto.message.MessageCreateRequest;
import com.example.campus.dto.message.MessageResponse;
import com.example.campus.dto.message.MessageUpdateRequest;
import com.example.campus.entity.TsukiMessage;
import com.example.campus.entity.TsukiUser;
import com.example.campus.exception.ResourceNotFoundException;
import com.example.campus.repository.TsukiMessageRepository;
import com.example.campus.repository.TsukiUserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final TsukiMessageRepository messageRepository;
    private final TsukiUserRepository userRepository;

    @Transactional(readOnly = true)
    public List<MessageResponse> findAll() {
        return messageRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> findByReceiverId(Long receiverId) {
        return messageRepository.findByReceiver_Id(receiverId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<MessageResponse> findBySenderId(Long senderId) {
        return messageRepository.findBySenderId(senderId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MessageResponse findById(Long id) {
        return toResponse(getMessage(id));
    }

    @Transactional
    public MessageResponse create(MessageCreateRequest request) {
        TsukiUser receiver = getUser(request.receiverId());
        TsukiMessage message = TsukiMessage.builder()
                .senderId(request.senderId())
                .receiver(receiver)
                .title(request.title())
                .content(request.content())
                .isRead(request.isRead() != null ? request.isRead() : Boolean.FALSE)
                .build();
        TsukiMessage saved = messageRepository.save(message);
        return toResponse(saved);
    }

    @Transactional
    public MessageResponse update(Long id, MessageUpdateRequest request) {
        TsukiMessage message = getMessage(id);
        if (request.title() != null) {
            message.setTitle(request.title());
        }
        if (request.content() != null) {
            message.setContent(request.content());
        }
        if (request.isRead() != null) {
            message.setIsRead(request.isRead());
        }
        return toResponse(message);
    }

    @Transactional
    public void delete(Long id) {
        TsukiMessage message = getMessage(id);
        messageRepository.delete(message);
    }

    private TsukiMessage getMessage(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的消息"));
    }

    private TsukiUser getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("未找到ID为" + id + "的用户"));
    }

    private MessageResponse toResponse(TsukiMessage message) {
        return new MessageResponse(
                message.getId(),
                message.getSenderId(),
                message.getReceiver().getId(),
                message.getTitle(),
                message.getContent(),
                message.getIsRead(),
                message.getSendTime());
    }
}
