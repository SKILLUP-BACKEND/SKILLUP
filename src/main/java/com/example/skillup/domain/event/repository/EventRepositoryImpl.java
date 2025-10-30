package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class EventRepositoryImpl implements EventRepositoryNative {

    private final EntityManager entityManager;
    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    static public class EventWithPopularity {
        private Event event;
        private Double popularity;
    }
    private LocalDateTime convertToLocalDateTime(Object value) {
        if (value == null) return null;
        else {
            if (value instanceof Timestamp ts) return ts.toLocalDateTime();
            if (value instanceof LocalDateTime dt) return dt;
        }
        return null;
    }
    @Override
    public List<EventWithPopularity> findByCategoryWithSearch(EventRequest.EventSearchCondition cond, Pageable pageable, LocalDate since, LocalDateTime now) {

        String baseQuery = """
        SELECT                e.id,e.title,
                              e.thumbnail_url,
                              e.category,
                              e.event_start,
                              e.event_end,
                              e.recruit_start,
                              e.recruit_end,
                              e.is_free,
                              e.price,
                              e.is_online,
                              e.location_text,
                              e.location_link,
                              e.apply_link,
                              e.status,
                              e.contact,
                              e.description,
                              e.views_count,
                              e.likes_count,
                              e.apply_clicks,
                              e.recommended_manual,
                              e.ad_flag,
               (
                coalesce(sum(v.cnt), 0) * 0.6
                + count(DISTINCT el.id) * 0.3
                + (
                    CASE WHEN coalesce(sum(v.cnt), 0) > 0
                            THEN (1.0 * count(distinct ea.id)  / coalesce(sum(v.cnt), 1))
                            ELSE 0
                    END
                ) * 0.1
               ) AS popularity
        FROM event e
        LEFT JOIN event_view_daily v ON v.event_id = e.id AND v.created_at >= :since
        LEFT JOIN event_like el ON el.event_id = e.id AND el.created_at >= :since
        LEFT JOIN event_action ea ON ea.event_id = e.id AND ea.created_at >= :since AND ea.action_type = 'APPLY'
        LEFT JOIN event_target_role etr ON etr.event_id = e.id
        LEFT JOIN target_role tr ON tr.id = etr.role_id
        WHERE (:category IS NULL OR e.category = :category)
          AND (e.event_end IS NULL OR e.event_end >= :now)
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
            default -> " ORDER BY popularity DESC";
        };

        Query query = entityManager.createNativeQuery(baseQuery + orderBy)
                .setParameter("category", cond.getCategory().name())
                .setParameter("isOnline", cond.getIsOnline())
                .setParameter("isFree", cond.getIsFree())
                .setParameter("startDate", cond.getStartDate())
                .setParameter("endDate", cond.getEndDate())
                .setParameter("targetRoles", cond.getTargetRoles())
                .setParameter("since", since)
                .setParameter("now", now);


        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());


        List<Object[]> resultList = query.getResultList();
        List<EventWithPopularity> results = new ArrayList<>();

        for (Object[] row : resultList) {
            Event event = Event.builder()
                    .id(((Number) row[0]).longValue())
                    .title((String) row[1])
                    .thumbnailUrl((String) row[2])
                    .category(EventCategory.valueOf((String) row[3]))
                    .eventStart(convertToLocalDateTime(row[4]))
                    .eventEnd(convertToLocalDateTime(row[5]))
                    .recruitStart(convertToLocalDateTime(row[6]))
                    .recruitEnd(convertToLocalDateTime(row[7]))
                    .isFree((Boolean) row[8])
                    .price(row[9] != null ? ((Number) row[9]).intValue() : null)
                    .isOnline((Boolean) row[10])
                    .locationText((String) row[11])
                    .locationLink((String) row[12])
                    .applyLink((String) row[13])
                    .status(EventStatus.valueOf((String) row[14]))
                    .contact((String) row[15])
                    .description((String) row[16])
                    .viewsCount(((Number) row[17]).longValue())
                    .likesCount(((Number) row[18]).longValue())
                    .applyClicks(((Number) row[19]).longValue())
                    .recommendedManual((Boolean) row[20])
                    .ad((Boolean) row[21])
                    .build();
            Double popularity = row[22] != null ? ((Number) row[22]).doubleValue() : 0.0;

            results.add(new EventWithPopularity(event, popularity));
        }
        return results;
    }
}

