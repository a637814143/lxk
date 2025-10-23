package com.example.campus.dto.student;

public record StudentResponse(
        Long id,
        Long userId,
        String name,
        String gender,
        String school,
        String major,
        String grade,
        String education,
        String avatar) {
}
