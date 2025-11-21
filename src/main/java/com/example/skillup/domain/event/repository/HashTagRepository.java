package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.HashTag;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HashTagRepository extends JpaRepository<HashTag,Long> {
    Optional<HashTag> findByName(String name);

}
