package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventAction;
import com.example.skillup.domain.event.enums.ActionType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface EventActionRepository extends JpaRepository<EventAction, Long> {

   // List<Event> findEventByHashTagScore();

    @Query("""
        SELECT ea.event
        FROM EventAction ea
        WHERE ea.actorId = :actorId AND ea.actionType = :actionType
        ORDER BY ea.updatedAt DESC
""")
    List<Event> findRecentEventsByActorId(@Param("actorId")String actorId, Pageable pageable , @Param("actionType")ActionType actionType);

    List<EventAction> findAllByEventAndActionType(Event event, ActionType actionType);

    Optional<EventAction> findByEventAndActorIdAndActionType(Event event, String actorId , ActionType actionType);


    @Query(value = """
    SELECT
        ea.actor_id      AS actorId,
        ea.created_at    AS createdAt,
        GROUP_CONCAT(tr.name ORDER BY tr.name SEPARATOR ',') AS targetRoles
    FROM event_action ea
    JOIN event e
        ON ea.event_id = e.id
    JOIN event_target_role etr
        ON e.id = etr.event_id
    JOIN target_role tr
        ON tr.id = etr.role_id
    WHERE ea.created_at >= :since
      AND ea.action_type = :actionType
    GROUP BY ea.id, ea.actor_id, ea.created_at
""", nativeQuery = true)
    List<EventActionAnalyticsProjection> findEventActionsBySinceAndActionType(
            @Param("since") LocalDateTime since,
            @Param("actionType") String actionType
    );


    public interface  EventActionAnalyticsProjection
    {
        String getActorId();
        LocalDateTime getCreatedAt();
        String getTargetRoles();
    }

}
