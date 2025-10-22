package com.example.campus.controller;

import com.example.campus.dto.resume.ResumeCreateRequest;
import com.example.campus.dto.resume.ResumeResponse;
import com.example.campus.dto.resume.ResumeUpdateRequest;
import com.example.campus.service.ResumeService;
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
@RequestMapping("/api/resumes")
@RequiredArgsConstructor
public class ResumeController {

    private final ResumeService resumeService;

    @GetMapping
    public List<ResumeResponse> list() {
        return resumeService.findAll();
    }

    @GetMapping("/student/{studentId}")
    public List<ResumeResponse> findByStudent(@PathVariable Long studentId) {
        return resumeService.findByStudentId(studentId);
    }

    @GetMapping("/{id}")
    public ResumeResponse get(@PathVariable Long id) {
        return resumeService.findById(id);
    }

    @PostMapping
    public ResponseEntity<ResumeResponse> create(@Valid @RequestBody ResumeCreateRequest request) {
        ResumeResponse response = resumeService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResumeResponse update(@PathVariable Long id, @Valid @RequestBody ResumeUpdateRequest request) {
        return resumeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        resumeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
