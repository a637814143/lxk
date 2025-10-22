package com.example.campus.controller;

import com.example.campus.dto.message.MessageCreateRequest;
import com.example.campus.dto.message.MessageResponse;
import com.example.campus.dto.message.MessageUpdateRequest;
import com.example.campus.service.MessageService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @GetMapping
    public List<MessageResponse> list() {
        return messageService.findAll();
    }

    @GetMapping("/{id}")
    public MessageResponse get(@PathVariable Long id) {
        return messageService.findById(id);
    }

    @PostMapping
    public ResponseEntity<MessageResponse> create(@Valid @RequestBody MessageCreateRequest request) {
        MessageResponse response = messageService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public MessageResponse update(@PathVariable Long id, @Valid @RequestBody MessageUpdateRequest request) {
        return messageService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        messageService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
