package com.example.skillup.domain.event.listener;

import com.example.skillup.domain.event.events.EventCreatedEvent;
import com.example.skillup.global.recovery.entity.SearchIndexFailure;
import com.example.skillup.global.recovery.repository.SearchIndexFailureRepository;
import com.example.skillup.global.recovery.service.SearchIndexRetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventIndexingListener {
    private final SearchIndexFailureRepository searchIndexFailureRepository;
    private final SearchIndexRetryService searchIndexRetryService;

    @Value("${skillup.search.index-name}")
    private String indexName;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void recordIndexingBeforeCommit(EventCreatedEvent event) {
        searchIndexFailureRepository.save(SearchIndexFailure.pending(event.eventId(), indexName));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void indexEventAfterCommit(EventCreatedEvent event) {
        try {
            searchIndexRetryService.processPendingEvent(event.eventId());
        } catch (Exception e) {
            // 커밋된 PENDING 작업은 스케줄러가 회수한다.
            log.error("커밋 후 검색 작업 실행 실패 - eventId={}", event.eventId(), e);
        }
    }
}
