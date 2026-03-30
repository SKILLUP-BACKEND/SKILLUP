package com.example.skillup.global.recovery.repository;

import com.example.skillup.global.recovery.entity.FileCleanupFailure;
import com.example.skillup.global.recovery.enums.RetryStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileCleanupFailureRepository extends JpaRepository<FileCleanupFailure, Long> {
    List<FileCleanupFailure> findTop100ByStatusOrderByCreatedAtAsc(RetryStatus status);
}
