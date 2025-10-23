package com.example.campus.dto.admin;

public record AdminResponse(
        Long id,
        Long userId,
        String name,
        Integer level) {
}
