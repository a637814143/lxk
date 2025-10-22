package com.example.campus.repository;

import com.example.campus.entity.TsukiStudent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiStudentRepository extends JpaRepository<TsukiStudent, Long> {
}
