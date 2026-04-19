package com.example.skillup.domain.event.listener;

import static com.example.skillup.global.common.CommonMapper.normalizeReason;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.events.EventCreatedEvent;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.global.recovery.enums.ResourceType;
import com.example.skillup.global.recovery.service.SearchIndexFailureSaveService;
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
    private final SearchIndexFailureSaveService searchIndexFailureSaveService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void indexEventAfterCommit(EventCreatedEvent event) {
        try {
            log.info("ES AFTER_COMMIT 실행 - eventId={}", event.eventId());

            Event savedEvent = eventRepository.findByIdWithHashTags(event.eventId())
                    .orElseThrow(() -> new RuntimeException("Event not found: " + event.eventId()));

            eventIndexerService.index(savedEvent);
        } catch (Exception e) {
            log.error("이벤트 인덱싱 실패 - eventId={}", event.eventId(), e);
            searchIndexFailureSaveService.saveFailure(
                    ResourceType.EVENT,
                    event.eventId(),
                    "events_v3",
                    String.valueOf(event.eventId()),
                    normalizeReason(e, "Elastic Search 인덱싱 실패")
            );
        }
    }
}
