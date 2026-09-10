package com.example.skillup.global.recovery.repository;

import com.example.skillup.global.recovery.entity.SearchIndexFailure;
import com.example.skillup.global.recovery.enums.ResourceType;
import com.example.skillup.global.recovery.enums.RetryStatus;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SearchIndexFailureRepository extends JpaRepository<SearchIndexFailure, Long> {
    List<SearchIndexFailure> findTop100ByStatusOrderByCreatedAtAsc(RetryStatus status);

    List<SearchIndexFailure> findTop100ByResourceTypeAndResourceIdAndStatusOrderByCreatedAtAsc(
            ResourceType resourceType, Long resourceId, RetryStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select task from SearchIndexFailure task where task.id = :id")
    Optional<SearchIndexFailure> findByIdForUpdate(@Param("id") Long id);
}
