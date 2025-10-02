package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.exception.EventErrorCode;
import com.example.skillup.domain.event.exception.EventException;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    default Event getEvent(Long eventId) {
        return findById(eventId).orElseThrow(() -> new EventException(EventErrorCode.EVENT_ENTITY_NOT_FOUND,  "EventID 가 " + eventId + "인"));
    }

    @Query("""
    select distinct e
    from Event e
    left join e.targetRoles tr
    where e.status = com.example.skillup.domain.event.enums.EventStatus.PUBLISHED
      and (e.eventEnd is null or e.eventEnd >= :now)
      and (:roleName is null or tr.name = :roleName)
    order by
        (e.viewsCount * 0.6)
      + (e.likesCount * 0.3)
      + (case when e.applyImpressions > 0
              then (1.0 * e.applyClicks / e.applyImpressions)
              else 0 end) * 0.1 desc,
        e.createdAt desc
    """)
    List<Event> findPopularForHome(@Param("roleName") String roleName,
                                   @Param("now") LocalDateTime now,
                                   Pageable pageable);

    @Query("""
        select distinct e
        from Event e
        left join e.targetRoles tr
        where e.status = com.example.skillup.domain.event.enums.EventStatus.PUBLISHED
          and (e.eventEnd is null or e.eventEnd >= :now)
          and e.recruitEnd is not null
          and e.recruitEnd between :now and :due
          and (:roleName is null or tr.name = :roleName)
        order by
            (e.viewsCount * 0.6)
          + (e.likesCount * 0.3)
          + (case when e.applyImpressions > 0
                  then (1.0 * e.applyClicks / e.applyImpressions)
                  else 0 end) * 0.1 desc,
            e.createdAt desc
    """)
    List<Event> findClosingSoonForHome(@Param("roleName") String roleName,
                                       @Param("now") LocalDateTime now,
                                       @Param("due") LocalDateTime due,
                                       Pageable pageable);

    @Query("""
    select distinct e
    from Event e
    left join e.targetRoles tr
    where e.status = com.example.skillup.domain.event.enums.EventStatus.PUBLISHED
      and e.category = com.example.skillup.domain.event.enums.EventCategory.BOOTCAMP_CLUB
      and (:roleName is null or tr.name = :roleName)
      and ( (e.recruitStart is null or e.recruitStart <= :now)
            and (e.recruitEnd   is null or e.recruitEnd   >= :now) )
    order by
        (e.viewsCount * 0.6)
      + (e.likesCount * 0.3)
      + (case when e.applyImpressions > 0
              then (1.0 * e.applyClicks / e.applyImpressions)
              else 0 end) * 0.1 desc,
        case when e.recruitEnd is null then 1 else 0 end,
        e.recruitEnd asc
""")
    List<Event> findRecruitingBootcamps(@Param("roleName") String roleName,
                                        @Param("now") LocalDateTime now,
                                        Pageable pageable);
}
