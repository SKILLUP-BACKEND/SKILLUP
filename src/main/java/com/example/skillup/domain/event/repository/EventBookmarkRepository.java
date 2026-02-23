package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventBookmark;
import com.example.skillup.domain.user.entity.Users;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventBookmarkRepository extends JpaRepository<EventBookmark, Long> {
    Optional<EventBookmark> findByUserAndEvent(Users user, Event event);



    @Query("""
        select eb.event
        from EventBookmark eb
        where eb.user = :user and eb.isBookmarked = true
        order by eb.createdAt desc
       """)
    Page<Event> findEventsByUserWithLatest(@Param("user") Users user,
                                           Pageable pageable);


    @Query("""
        select eb.event
        from EventBookmark eb
        where eb.user = :user and eb.isBookmarked = true
        order by eb.event.recruitEnd asc
       """)
    Page<Event> findEventsByUserWithDeadLine(@Param("user") Users user,
                              Pageable pageable);


    @Query("""
        select eb.event.id
        from EventBookmark eb
        where eb.user.id = :userId
          and eb.event.id in :eventIds
          and eb.isBookmarked = true
    """)
    List<Long> findBookmarkedEventIds(@Param("userId") Long userId,
                                      @Param("eventIds") List<Long> eventIds);

    @Query(value = """
    SELECT
        CAST(eb.user_id AS CHAR) AS actorId,
        eb.created_at            AS createdAt,
        GROUP_CONCAT(tr.name ORDER BY tr.name SEPARATOR ',') AS targetRoles
    FROM event_bookmark eb
    JOIN event e
        ON eb.event_id = e.id AND e.deleted_at IS NULL
    JOIN event_target_role etr
        ON e.id = etr.event_id
    JOIN target_role tr
        ON tr.id = etr.role_id
    WHERE eb.created_at >= :since
      AND eb.deleted_at IS NULL
      AND eb.is_bookmarked = true
    GROUP BY eb.id, eb.user_id, eb.created_at
""", nativeQuery = true)
    List<EventActionRepository.EventActionAnalyticsProjection> findBookmarkAnalyticsSince(
            @Param("since") LocalDateTime since
    );
}
