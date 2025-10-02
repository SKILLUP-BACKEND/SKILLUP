package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.exception.EventErrorCode;
import com.example.skillup.domain.event.exception.EventException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {
    default Event getEvent(Long eventId) {
        return findById(eventId).orElseThrow(() -> new EventException(EventErrorCode.EVENT_ENTITY_NOT_FOUND,  "EventID 가 " + eventId + "인"));
    }

    Page<Event> findAllByCategoryIn(Set<EventCategory> categories, Pageable pageable);
}
