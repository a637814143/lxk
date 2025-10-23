package com.example.campus.repository;

import com.example.campus.entity.TsukiStudent;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiStudentRepository extends JpaRepository<TsukiStudent, Long> {

    Optional<TsukiStudent> findByUser_Id(Long userId);
}
