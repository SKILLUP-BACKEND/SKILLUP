package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.exception.EventErrorCode;
import com.example.skillup.domain.event.exception.EventException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryNative {
    default Event getEvent(Long eventId) {
        return findById(eventId).orElseThrow(
                () -> new EventException(EventErrorCode.EVENT_ENTITY_NOT_FOUND, "EventID 가 " + eventId + "인"));
    }

    Page<Event> findAllByCategoryIn(Set<EventCategory> categories, Pageable pageable);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "UPDATE event SET views_count = views_count + 1 , updated_at = CURRENT_TIMESTAMP WHERE id = :eventId", nativeQuery = true)
    void incrementViews(@Param("eventId") Long eventId);

    @Modifying(clearAutomatically = true)
    @Query(value = "UPDATE event SET apply_clicks = apply_clicks + 1 , updated_at = CURRENT_TIMESTAMP WHERE id = :eventId", nativeQuery = true)
    void incrementApplys(@Param("eventId") Long eventId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
            UPDATE event
               SET bookmarked_count = GREATEST(bookmarked_count + :delta, 0) ,  updated_at = CURRENT_TIMESTAMP
             WHERE id = :eventId
            """, nativeQuery = true)
    int incrementBookmarks(@Param("eventId") Long eventId, @Param("delta") int delta);

    @Query("""
            select
                e as event,
                coalesce(sum(v.cnt), 0) as views14,
                count(distinct eb.id) as bookmarksCnt,
                (
                    coalesce(sum(v.cnt), 0) * 0.6
                  + count(distinct eb.id) * 0.3
                  + (
                        case when coalesce(sum(v.cnt), 0) > 0
                             then (1.0 * count(distinct ea.id) / coalesce(sum(v.cnt), 0))
                             else 0
                        end
                    ) * 0.1
                ) as popularity
            from Event e
            left join EventViewDaily v
                   on v.event = e and v.createdAt >= :since
            left join EventBookmark eb
                   on eb.event = e and eb.createdAt >= :since and eb.isBookmarked = true
            left join EventAction ea
                   on ea.event = e and ea.createdAt >= :since and ea.actionType = 'APPLY'
            where e.status = com.example.skillup.domain.event.enums.EventStatus.PUBLISHED
              and (e.eventEnd is null or e.eventEnd >= :now)
              and (
                    :roleName is null
                    or exists (
                        select 1
                        from Event e2 join e2.targetRoles tr2
                        where e2 = e and tr2.name = :roleName
                    )
              )
            group by e
            order by
                (
                    coalesce(sum(v.cnt), 0) * 0.6
                  + count(distinct eb.id) * 0.3
                  + (
                        case when coalesce(sum(v.cnt), 0) > 0
                             then (1.0 * count(distinct ea.id) / coalesce(sum(v.cnt), 0))
                             else 0
                        end
                    ) * 0.1
                ) desc,
                e.createdAt desc
            """)
    List<PopularEventProjection> findPopularForHomeWithPopularity(@Param("roleName") String roleName,
                                                                  @Param("since") LocalDateTime since,
                                                                  @Param("now") LocalDateTime now,
                                                                  Pageable pageable);


    @Query("""
            select distinct e
            from Event e
            join e.targetRoles tr
            where e.status = com.example.skillup.domain.event.enums.EventStatus.PUBLISHED
              and e.eventEnd >= :now
              and e.eventEnd <= :due
              and (:roleName is null or tr.name = :roleName)
            order by e.recruitEnd asc, e.createdAt desc
            """)
    List<Event> findClosingSoonForHome(
            @Param("roleName") String roleName,
            @Param("now") LocalDateTime now,
            @Param("due") LocalDateTime due,
            Pageable pageable
    );

    @Query("""
            select
                e as event,
                coalesce(sum(v.cnt), 0) as views14,
                count(distinct eb.id) as bookmarksCnt,
                (
                    coalesce(sum(v.cnt), 0) * 0.6
                  + count(distinct eb.id) * 0.3
                  + (
                        case when coalesce(sum(v.cnt), 0) > 0
                             then (1.0 * count(distinct ea.id) / coalesce(sum(v.cnt), 0))
                             else 0
                        end
                    ) * 0.1
                ) as popularity
            from Event e
            left join EventViewDaily v
                   on v.event = e and v.createdAt >= :since
            left join EventBookmark eb
                   on eb.event = e and eb.isBookmarked = true and eb.updatedAt >= :since
            left join EventAction ea
                   on ea.event = e and ea.createdAt >= :since and ea.actionType = 'APPLY'
            where e.status = com.example.skillup.domain.event.enums.EventStatus.PUBLISHED
              and e.category = com.example.skillup.domain.event.enums.EventCategory.BOOTCAMP_CLUB
              and (e.eventEnd is null or e.eventEnd >= :now)
              and e.recruitEnd is not null
              and e.recruitEnd >= :now
              and (
                    :roleName is null
                    or exists (
                        select 1
                        from Event e2 join e2.targetRoles tr2
                        where e2 = e and tr2.name = :roleName
                    )
              )
            group by e
            order by
                  (
                    coalesce(sum(v.cnt), 0) * 0.6
                    + count(distinct eb.id) * 0.3
                    + (
                        case when coalesce(sum(v.cnt), 0) > 0
                             then (1.0 * e.applyClicks / coalesce(sum(v.cnt), 0))
                             else 0
                        end
                    ) * 0.1
                ) desc, e.recruitEnd asc, e.createdAt desc
            """)
    List<PopularEventProjection> findBootcampsOpenOrderByPopularityWithPopularity(@Param("since") LocalDateTime since,
                                                                                  @Param("now") LocalDateTime now,
                                                                                  @Param("roleName") String roleName,
                                                                                  Pageable pageable);


    @Query("""
            select
                e as event,
                coalesce(sum(v.cnt), 0) as views14,
                count(distinct eb.id) as bookmarksCnt,
                (
                    coalesce(sum(v.cnt), 0) * 0.6
                  + count(distinct eb.id) * 0.3
                  + (
                        case when coalesce(sum(v.cnt), 0) > 0
                             then (1.0 * count(distinct ea.id) / coalesce(sum(v.cnt), 0))
                             else 0
                        end
                    ) * 0.1
                ) as popularity
            from Event e
            left join EventViewDaily v
                   on v.event = e and v.createdAt >= :since
            left join EventBookmark eb
                   on eb.event = e and eb.isBookmarked = true and eb.updatedAt >= :since
            left join EventAction ea
                   on ea.event = e and ea.createdAt >= :since and ea.actionType = 'APPLY'
            where e.status = com.example.skillup.domain.event.enums.EventStatus.PUBLISHED
              and e.category = :category
              and (e.eventEnd is null or e.eventEnd >= :now)
              and e.recruitEnd is not null
              and e.recruitEnd between :now and :due
            group by e
            order by
                   (
                    coalesce(sum(v.cnt), 0) * 0.6
                    + count(distinct eb.id) * 0.3
                    + (
                        case when coalesce(sum(v.cnt), 0) > 0
                             then (1.0 * e.applyClicks / coalesce(sum(v.cnt), 0))
                             else 0
                        end
                    ) * 0.1
                ) desc, e.recruitEnd asc, e.createdAt desc
            """)
    List<PopularEventProjection> findByCategoryWithin30DaysOrderByPopularityWithPopularity(
            @Param("category") EventCategory category,
            @Param("since") LocalDateTime since,
            @Param("now") LocalDateTime now,
            @Param("due") LocalDateTime due,
            Pageable pageable);

    public interface PopularEventProjection {
        Event getEvent();
        Long getViews14();
        Long getBookmarksCnt();
        Double getPopularity();
    }

    @Query(value = """
            SELECT e.*
            FROM `event` e
            JOIN event_hash_tags eht ON e.id = eht.event_id
            JOIN (
                SELECT activity_tag_weights.hash_tags_id,
                       SUM(activity_tag_weights.weight) AS score
                FROM (
                    SELECT eht2.hash_tags_id AS hash_tags_id,
                           0.3E0 AS weight
                    FROM event_action ea
                    JOIN event_hash_tags eht2 ON ea.event_id = eht2.event_id
                    WHERE ea.actor_id = :actorId
                      AND ea.created_at >= :since
                      AND ea.action_type = 'VIEW'
            
                    UNION ALL
            
                    -- 북마크 점수 환산
                    SELECT eht3.hash_tags_id AS hash_tags_id,
                           0.6E0 AS weight
                    FROM event_bookmark eb
                    JOIN event_hash_tags eht3 ON eb.event_id = eht3.event_id
                    WHERE eb.user_id = :userId
                      AND eb.deleted_at IS NULL
                      AND eb.is_bookmarked = true
                      AND eb.updated_at >= :since
            
                    UNION ALL
            
                    SELECT eht4.hash_tags_id AS hash_tags_id,
                           0.1E0 AS weight
                    FROM event_action ea2
                    JOIN event_hash_tags eht4 ON ea2.event_id = eht4.event_id
                    WHERE ea2.actor_id = :actorId
                      AND ea2.created_at >= :since
                      AND ea2.action_type = 'APPLY'
                ) AS activity_tag_weights
                GROUP BY activity_tag_weights.hash_tags_id
                ORDER BY score DESC
                LIMIT 10
            ) AS top_tags ON eht.hash_tags_id = top_tags.hash_tags_id
            WHERE e.id NOT IN (
                -- 북마크 및 이미 보거나 신청한 행사 제외
                SELECT ea3.event_id
                FROM event_action ea3
                WHERE ea3.actor_id = :actorId
                AND ea3.action_type = 'APPLY'
            
                UNION
            
                SELECT eb2.event_id
                FROM event_bookmark eb2
                WHERE eb2.user_id = :userId
                  AND eb2.deleted_at IS NULL
                  AND eb2.is_bookmarked = true
            )
            GROUP BY e.id
            ORDER BY SUM(top_tags.score) DESC
            LIMIT 6
            """, nativeQuery = true)
    List<Event> findRecommendedEventForHome(@Param("actorId") String actorId, @Param("userId") Long userId,
                                            @Param("since") LocalDateTime since);


    @Query(value = """
                SELECT COUNT(*)
                FROM (
                    SELECT e.id
                    FROM event e
                    LEFT JOIN event_target_role etr ON etr.event_id = e.id
                    LEFT JOIN target_role tr ON tr.id = etr.role_id
                    WHERE (:category IS NULL OR e.category = :category)
                      AND (e.event_end IS NULL OR e.event_end >= :now)
                      AND (e.status = 'PUBLISHED')
                      AND (:isOnline IS NULL OR e.is_online = :isOnline)
                      AND (:isFree IS NULL OR e.is_free = :isFree)
                      AND (:startDate IS NULL OR e.event_start BETWEEN :startDate AND :endDate)
                      AND (
                            :targetRolesIsEmpty = TRUE
                            OR tr.name = :targetRole
                      )
                    GROUP BY e.id
                ) AS counted
            """, nativeQuery = true)
    int countByCategoryWithSearch(
            @Param("category") String category,
            @Param("isOnline") Boolean isOnline,
            @Param("isFree") Boolean isFree,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("targetRole") String targetRole,
            @Param("now") LocalDateTime now,
            @Param("targetRolesIsEmpty") Boolean targetRolesIsEmpty
    );
}
