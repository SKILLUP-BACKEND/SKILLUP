package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.EventLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventLikeRepository extends JpaRepository<EventLike, Long> {
    boolean existsByEventIdAndUserId(Long eventId, Long userId);

    void deleteByEventIdAndUserId(Long eventId, Long userId);

    @Modifying
    @Query("DELETE FROM EventLike el WHERE el.event.id = :eventId")
    void deleteAllByEventId(@Param("eventId") Long eventId);
}
