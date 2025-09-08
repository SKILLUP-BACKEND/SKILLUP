package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.TargetRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TargetRoleRepository extends JpaRepository<TargetRole, Long> {
    Optional<TargetRole> findByName(String name);
}
