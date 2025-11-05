package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.exception.EventErrorCode;
import com.example.skillup.domain.event.exception.EventException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long>, EventRepositoryNative {
    default Event getEvent(Long eventId) {
        return findById(eventId).orElseThrow(() -> new EventException(EventErrorCode.EVENT_ENTITY_NOT_FOUND,  "EventID 가 " + eventId + "인"));
    }

    Page<Event> findAllByCategoryIn(Set<EventCategory> categories, Pageable pageable);

    @Modifying
    @Query(value = "UPDATE event SET views_count = views_count + 1 WHERE id = :eventId", nativeQuery = true)
    void incrementViews(@Param("eventId") Long eventId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = """
        UPDATE event
           SET likes_count = GREATEST(likes_count + :delta, 0)
         WHERE id = :eventId
        """, nativeQuery = true)
    int incrementLikes(@Param("eventId") Long eventId, @Param("delta") int delta);

    @Query("""
    select
        e as event,
        coalesce(sum(v.cnt), 0) as views14,
        count(distinct el.id) as likesCnt,
        (
            coalesce(sum(v.cnt), 0) * 0.6
          + count(distinct el.id) * 0.3
          + (
                case when coalesce(sum(v.cnt), 0) > 0
                     then (1.0 * e.applyClicks / coalesce(sum(v.cnt), 0))
                     else 0
                end
            ) * 0.1
        ) as popularity
    from Event e
    left join EventViewDaily v
           on v.event = e and v.createdAt >= :since
    left join EventLike el
           on el.event = e and el.createdAt >= :since
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
          + count(distinct el.id) * 0.3
          + (
                case when coalesce(sum(v.cnt), 0) > 0
                     then (1.0 * e.applyClicks / coalesce(sum(v.cnt), 0))
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
    select
        e as event,
        coalesce(sum(v.cnt), 0) as views14,
        count(distinct el.id) as likesCnt,
        (
            coalesce(sum(v.cnt), 0) * 0.6
          + count(distinct el.id) * 0.3
          + (
                case when coalesce(sum(v.cnt), 0) > 0
                     then (1.0 * e.applyClicks / coalesce(sum(v.cnt), 0))
                     else 0
                end
            ) * 0.1
        ) as popularity
    from Event e
    left join EventViewDaily v
           on v.event = e and v.createdAt >= :since
    left join EventLike el
           on el.event = e
    where e.status = com.example.skillup.domain.event.enums.EventStatus.PUBLISHED
      and (e.eventEnd is null or e.eventEnd >= :now)
      and e.recruitEnd is not null
      and e.recruitEnd between :now and :due
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
            + count(distinct el.id) * 0.3
            + (
                case when coalesce(sum(v.cnt), 0) > 0
                     then (1.0 * e.applyClicks / coalesce(sum(v.cnt), 0))
                     else 0
                end
            ) * 0.1
        ) desc, e.recruitEnd asc, e.createdAt desc
    """)
    List<PopularEventProjection> findClosingSoonForHomeWithPopularity(@Param("roleName") String roleName,
                                                                      @Param("since") LocalDateTime since,
                                                                      @Param("now") LocalDateTime now,
                                                                      @Param("due") LocalDateTime due,
                                                                      Pageable pageable);

    @Query("""
    select
        e as event,
        coalesce(sum(v.cnt), 0) as views14,
        count(distinct el.id) as likesCnt,
        (
            coalesce(sum(v.cnt), 0) * 0.6
          + count(distinct el.id) * 0.3
          + (
                case when coalesce(sum(v.cnt), 0) > 0
                     then (1.0 * e.applyClicks / coalesce(sum(v.cnt), 0))
                     else 0
                end
            ) * 0.1
        ) as popularity
    from Event e
    left join EventViewDaily v
           on v.event = e and v.createdAt >= :since
    left join EventLike el
           on el.event = e
    where e.status = com.example.skillup.domain.event.enums.EventStatus.PUBLISHED
      and e.category = com.example.skillup.domain.event.enums.EventCategory.BOOTCAMP_CLUB
      and (e.eventEnd is null or e.eventEnd >= :now)
      and e.recruitEnd is not null
      and e.recruitEnd >= :now
    group by e
    order by
          (
            coalesce(sum(v.cnt), 0) * 0.6
            + count(distinct el.id) * 0.3
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
                                                                                  Pageable pageable);


    @Query("""
    select
        e as event,
        coalesce(sum(v.cnt), 0) as views14,
        count(distinct el.id) as likesCnt,
        (
            coalesce(sum(v.cnt), 0) * 0.6
          + count(distinct el.id) * 0.3
          + (
                case when coalesce(sum(v.cnt), 0) > 0
                     then (1.0 * e.applyClicks / coalesce(sum(v.cnt), 0))
                     else 0
                end
            ) * 0.1
        ) as popularity
    from Event e
    left join EventViewDaily v
           on v.event = e and v.createdAt >= :since
    left join EventLike el
           on el.event = e
    where e.status = com.example.skillup.domain.event.enums.EventStatus.PUBLISHED
      and e.category = :category
      and (e.eventEnd is null or e.eventEnd >= :now)
      and e.recruitEnd is not null
      and e.recruitEnd between :now and :due
    group by e
    order by
           (
            coalesce(sum(v.cnt), 0) * 0.6
            + count(distinct el.id) * 0.3
            + (
                case when coalesce(sum(v.cnt), 0) > 0
                     then (1.0 * e.applyClicks / coalesce(sum(v.cnt), 0))
                     else 0
                end
            ) * 0.1
        ) desc, e.recruitEnd asc, e.createdAt desc
    """)
    List<PopularEventProjection> findByCategoryWithin30DaysOrderByPopularityWithPopularity(@Param("category") EventCategory category,
                                                                                           @Param("since") LocalDateTime since,
                                                                                           @Param("now") LocalDateTime now,
                                                                                           @Param("due") LocalDateTime due,
                                                                                           Pageable pageable);

    public interface PopularEventProjection {
        Event getEvent();
        Long getViews14();
        Long getLikesCnt();
        Double getPopularity();
    }
    // 위에는 점수까지 포함(test 용) 아래는 점수 포함하지 않은 쿼리문
    @Query("""
    select e
    from Event e
    left join EventViewDaily v
           on v.event = e and v.createdAt >= :since
    left join EventLike el
           on el.event = e
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
        coalesce(sum(v.cnt), 0) * 0.6
      + count(distinct el.id) * 0.3
      + (
            case when coalesce(sum(v.cnt), 0) > 0
                 then (1.0 * e.applyClicks / coalesce(sum(v.cnt), 0))
                 else 0
            end
        ) * 0.1
      desc , e.createdAt desc
    """)
    List<Event> findPopularForHome(@Param("roleName") String roleName,
                                   @Param("since") LocalDate since,
                                   @Param("now") LocalDateTime now,
                                   Pageable pageable);

    @Query("""
    select e
    from Event e
    left join EventViewDaily v
           on v.event = e and v.createdAt >= :since
    left join EventLike el
           on el.event = e
    where e.status = com.example.skillup.domain.event.enums.EventStatus.PUBLISHED
      and (e.eventEnd is null or e.eventEnd >= :now)
      and e.recruitEnd is not null
      and e.recruitEnd between :now and :due
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
        coalesce(sum(v.cnt), 0) * 0.6
      + count(distinct el.id) * 0.3
      + (
            case when coalesce(sum(v.cnt), 0) > 0
                 then (1.0 * e.applyClicks / coalesce(sum(v.cnt), 0))
                 else 0
            end
        ) * 0.1
      desc,
      e.createdAt desc
    """)
    List<Event> findClosingSoonForHome(@Param("roleName") String roleName,
                                       @Param("since") LocalDate since,
                                       @Param("now") LocalDateTime now,
                                       @Param("due") LocalDateTime due,
                                       Pageable pageable);

    @Query("""
    select e
    from Event e
    left join EventViewDaily v
           on v.event = e and v.createdAt >= :since
    left join EventLike el
           on el.event = e
    where e.status = com.example.skillup.domain.event.enums.EventStatus.PUBLISHED
      and e.category = com.example.skillup.domain.event.enums.EventCategory.BOOTCAMP_CLUB
      and (e.eventEnd is null or e.eventEnd >= :now)
      and e.recruitEnd is not null
      and e.recruitEnd >= :now
    group by e
    order by
        coalesce(sum(v.cnt), 0) * 0.6
      + count(distinct el.id) * 0.3
      + (
            case when coalesce(sum(v.cnt), 0) > 0
                 then (1.0 * e.applyClicks / coalesce(sum(v.cnt), 0))
                 else 0
            end
        ) * 0.1
      desc,
      e.recruitEnd asc,
      e.createdAt desc
    """)
    List<Event> findBootcampsOpenOrderByPopularity(@Param("since") LocalDate since,
                                                   @Param("now") LocalDateTime now,
                                                   Pageable pageable);

    @Query("""
    select e
    from Event e
    left join EventViewDaily v
           on v.event = e and v.createdAt >= :since
    left join EventLike el
           on el.event = e
    where e.status = com.example.skillup.domain.event.enums.EventStatus.PUBLISHED
      and e.category = :category
      and (e.eventEnd is null or e.eventEnd >= :now)
      and e.recruitEnd is not null
      and e.recruitEnd between :now and :due
    group by e
    order by
        coalesce(sum(v.cnt), 0) * 0.6
      + count(distinct el.id) * 0.3
      + (
            case when coalesce(sum(v.cnt), 0) > 0
                 then (1.0 * e.applyClicks / coalesce(sum(v.cnt), 0))
                 else 0
            end
        ) * 0.1
      desc,
      e.recruitEnd asc,
      e.createdAt desc
    """)
    List<Event> findByCategoryWithin30DaysOrderByPopularity(@Param("category") EventCategory category,
                                                            @Param("since") LocalDate since,
                                                            @Param("now") LocalDateTime now,
                                                            @Param("due") LocalDateTime due,
                                                            Pageable pageable);

    //


}
