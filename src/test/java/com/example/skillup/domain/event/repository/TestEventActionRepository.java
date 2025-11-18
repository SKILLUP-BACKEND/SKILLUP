package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.EventAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TestEventActionRepository extends JpaRepository<EventAction, Long> {


    @Query(value = """

SELECT ea.action_type AS actionType,
       GROUP_CONCAT(ht.id) AS tagIds
FROM event_action ea
JOIN event_hash_tags eht
    ON ea.event_id = eht.event_id
JOIN hash_tag ht
    ON ht.id = eht.hash_tags_id
WHERE ea.actor_id = :actorId
GROUP BY ea.id
""", nativeQuery = true)
    List<EventActionTagProjection> findEventActionByActorId(@Param("actorId") String actorId);

    public interface EventActionTagProjection {
        String getActionType();
        String getTagIds();
    }


    @Query("""
SELECT DISTINCT ea
FROM EventAction ea
JOIN FETCH ea.event e
JOIN FETCH e.hashTags ht
WHERE ea.actorId = :actorId
""")
    List<EventAction> findEventActionByActorIdJpa(@Param("actorId") String actorId);
}
