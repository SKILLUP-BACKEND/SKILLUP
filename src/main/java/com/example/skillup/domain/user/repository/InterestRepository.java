package com.example.skillup.domain.user.repository;

import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.user.entity.Interest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface InterestRepository extends JpaRepository<Interest, Long>
{
    List<Interest> findByRole(TargetRole role);
    Set<Interest> findByNameIn(List<String> names);
}
