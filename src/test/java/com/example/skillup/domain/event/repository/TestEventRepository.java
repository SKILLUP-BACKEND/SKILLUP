package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestEventRepository extends JpaRepository<Event, Long> {

    @Query(value = """
    SELECT 
        e.id AS eventId,
        GROUP_CONCAT(eht.hash_tags_id) AS tagIds
    FROM event e
    JOIN event_hash_tags eht ON e.id = eht.event_id
    WHERE e.id NOT IN (
        SELECT ea.event_id
        FROM event_action ea
        WHERE ea.actor_id = :actorId
    )
    GROUP BY e.id
    """, nativeQuery = true)
    List<EventTagProjection> findAllNotViewedByUser(
            @Param("actorId") String actorId
    );

    public interface EventTagProjection {
        Long getEventId();
        String getTagIds();
    }

    @Query(value = "SELECT * FROM event WHERE id IN (:ids)", nativeQuery = true)
    List<Event> findAllByIds(@Param("ids") List<Long> ids);

    @Query("""
SELECT DISTINCT e
FROM Event e
LEFT JOIN FETCH e.hashTags ht
WHERE e.id NOT IN (
    SELECT ea.event.id
    FROM EventAction ea
    WHERE ea.actorId = :actorId
)
""")
    List<Event> findAllNotViewedByUserJpa(@Param("actorId") String actorId);
}
