package com.example.skillup.global.recovery.repository;

import com.example.skillup.global.recovery.entity.SearchIndexFailure;
import com.example.skillup.global.recovery.enums.RetryStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SearchIndexFailureRepository extends JpaRepository<SearchIndexFailure, Long> {
    List<SearchIndexFailure> findTop100ByStatusOrderByCreatedAtAsc(RetryStatus status);
}
