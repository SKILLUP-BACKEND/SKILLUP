package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.HashTag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HashTagRepository extends JpaRepository<HashTag,Long> {
    Optional<HashTag> findByName(String name);

}
