package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.entity.Event;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepositoryNative {

    private final EntityManager entityManager;

    @Override
    public List<Event> searchEvents(EventRequest.EventSearchCondition cond, Pageable pageable, LocalDate since) {
        String baseQuery = """
        SELECT e.*
        FROM event e
        LEFT JOIN event_view_daily v ON v.event_id = e.id AND v.created_at >= :since
        LEFT JOIN event_like el ON el.event_id = e.id AND el.created_at >= :since
        LEFT JOIN event_target_role etr ON etr.event_id = e.id
        LEFT JOIN target_role tr ON tr.id = etr.role_id
        WHERE (:category IS NULL OR e.category = :category)
          AND (e.status = 'PUBLISHED')
          AND (:isOnline IS NULL OR e.is_online = :isOnline)
          AND (:isFree IS NULL OR e.is_free = :isFree)
          AND (:startDate IS NULL OR e.event_start BETWEEN :startDate AND :endDate)
          AND (:targetRoles IS NULL OR tr.name IN (:targetRoles))
        GROUP BY e.id
        """;

        String orderBy = switch (cond.getSort()) {
            case "latest" -> " ORDER BY e.created_at DESC";
            case "deadline" -> " ORDER BY e.recruit_end ASC";
            default -> """
            ORDER BY (
              coalesce(sum(v.cnt), 0) * 0.6
              + count(DISTINCT el.id) * 0.3
              + (
                  CASE WHEN coalesce(sum(v.cnt), 0) > 0
                       THEN (1.0 * e.apply_clicks / coalesce(sum(v.cnt), 1))
                       ELSE 0
                  END
              ) * 0.1
            ) DESC
            """;
        };

        Query query = entityManager.createNativeQuery(baseQuery + orderBy, Event.class)
                .setParameter("category", cond.getCategory().name())
                .setParameter("isOnline", cond.getIsOnline())
                .setParameter("isFree", cond.getIsFree())
                .setParameter("startDate", cond.getStartDate())
                .setParameter("endDate", cond.getEndDate())
                .setParameter("targetRoles", cond.getTargetRoles())
                .setParameter("since", since);

        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());

        return query.getResultList();
    }
}

