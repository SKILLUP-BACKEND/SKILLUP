package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.entity.Event;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface EventRepositoryNative {
    List<Event> searchEvents(EventRequest.EventSearchCondition cond, Pageable pageable, LocalDate date, LocalDateTime now);
}
