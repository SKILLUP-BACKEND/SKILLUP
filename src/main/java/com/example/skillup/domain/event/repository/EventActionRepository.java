package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventAction;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventActionRepository extends JpaRepository<EventAction, Long> {

   // List<Event> findEventByHashTagScore();

    @Query("""
        SELECT ea.event
        FROM EventAction ea
        WHERE ea.actorId = :actorId
        ORDER BY ea.createdAt DESC
""")
    List<Event> findRecentEventsByActorId(@Param("actorId")String actorId, Pageable pageable);
}
