package com.example.campus.repository;

import com.example.campus.entity.TsukiUser;
import com.example.campus.entity.UserRole;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TsukiUserRepository extends JpaRepository<TsukiUser, Long> {
    Optional<TsukiUser> findByUsernameIgnoreCase(String username);

    Optional<TsukiUser> findByEmailIgnoreCase(String email);

    long countByRole(UserRole role);

    long countByRoleAndStatus(UserRole role, Integer status);
}
