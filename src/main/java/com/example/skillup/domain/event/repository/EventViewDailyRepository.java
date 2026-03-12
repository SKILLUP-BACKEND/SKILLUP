package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventViewDaily;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface EventViewDailyRepository extends JpaRepository<EventViewDaily, Long> {
    // 오늘 카운트 +1 (없으면 insert, 있으면 cnt+1)
    @Modifying
    @Query(value = """
            INSERT INTO event_view_daily(event_id, view_date, cnt , created_at, updated_at)
            VALUES (:eventId, CURRENT_DATE, 1 , CURRENT_TIMESTAMP , CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE cnt = cnt + 1,
                updated_at = CURRENT_TIMESTAMP
            """, nativeQuery = true)
    void upsertToday(@Param("eventId") Long eventId);

    // 특정 날짜 +1 (테스트/리플레이용)
    @Modifying
    @Query(value = """
            INSERT INTO event_view_daily(event_id, view_date, cnt)
            VALUES (:eventId, :date, 1)
            ON DUPLICATE KEY UPDATE cnt = cnt + 1
            """, nativeQuery = true)
    void upsertOnDate(@Param("eventId") Long eventId, @Param("date") LocalDate date);

    // 최근 14일 합계
    @Query(value = """
            SELECT COALESCE(SUM(cnt), 0)
            FROM event_view_daily
            WHERE event_id = :eventId
              AND createdAt >= CURRENT_DATE - INTERVAL 13 DAY
            """, nativeQuery = true)
    long sumLast14Days(@Param("eventId") Long eventId);

    Optional<EventViewDaily> findByEventAndViewDate(Event event, LocalDate viewDate);

    @Modifying
    @Query("DELETE FROM EventViewDaily v WHERE v.event.id = :eventId")
    void deleteAllByEventId(@Param("eventId") Long eventId);
}
