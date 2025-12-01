package com.example.skillup.domain.user.repository;

import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.user.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.awt.*;
import java.util.Optional;

public interface InterestRepository extends JpaRepository<Interest, Long>
{
    Optional<Interest> findByRole(TargetRole role);

}
