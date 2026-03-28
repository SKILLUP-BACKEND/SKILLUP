package com.example.skillup.domain.event.listener;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.events.EventCreatedEvent;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.global.search.service.EventIndexerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventIndexingListener {
    private final EventIndexerService eventIndexerService;
    private final EventRepository eventRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void indexEventAfterCommit(EventCreatedEvent event) {

        log.info("AFTER_COMMIT 실행 - eventId={}", event.eventId());

        Event savedEvent = eventRepository.getEvent(event.eventId());

        eventIndexerService.index(savedEvent);
    }
}
