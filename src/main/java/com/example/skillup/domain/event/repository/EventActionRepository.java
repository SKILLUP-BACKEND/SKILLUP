package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventAction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventActionRepository extends JpaRepository<EventAction, Long> {

    List<Event> findEventByHashTagScore();
}
