package com.example.campus.controller;

import com.example.campus.dto.admin.AdminCreateRequest;
import com.example.campus.dto.admin.AdminResponse;
import com.example.campus.dto.admin.AdminUpdateRequest;
import com.example.campus.service.AdminService;
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
@RequestMapping("/api/admins")
@RequiredArgsConstructor
public class AdminController {

    private final AdminService adminService;

    @GetMapping
    public List<AdminResponse> list() {
        return adminService.findAll();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<AdminResponse> findByUser(@PathVariable Long userId) {
        return adminService.findByUserId(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public AdminResponse get(@PathVariable Long id) {
        return adminService.findById(id);
    }

    @PostMapping
    public ResponseEntity<AdminResponse> create(@Valid @RequestBody AdminCreateRequest request) {
        AdminResponse response = adminService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public AdminResponse update(@PathVariable Long id, @Valid @RequestBody AdminUpdateRequest request) {
        return adminService.update(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        adminService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
