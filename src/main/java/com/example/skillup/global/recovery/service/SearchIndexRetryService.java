package com.example.skillup.global.recovery.service;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.global.common.CommonMapper;
import com.example.skillup.global.recovery.entity.SearchIndexFailure;
import com.example.skillup.global.recovery.enums.ResourceType;
import com.example.skillup.global.recovery.enums.RetryStatus;
import com.example.skillup.global.recovery.repository.SearchIndexFailureRepository;
import com.example.skillup.global.search.service.EventIndexerService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchIndexRetryService {
    private static final int MAX_RETRY_COUNT = 3;

    private final SearchIndexFailureRepository searchIndexFailureRepository;
    private final EventRepository eventRepository;
    private final EventIndexerService eventIndexerService;
    private final PlatformTransactionManager transactionManager;

    public void retryPendingFailures() {
        process(searchIndexFailureRepository.findTop100ByStatusOrderByCreatedAtAsc(RetryStatus.PENDING));
    }

    public void processPendingEvent(Long eventId) {
        process(searchIndexFailureRepository.findTop100ByResourceTypeAndResourceIdAndStatusOrderByCreatedAtAsc(
                ResourceType.EVENT, eventId, RetryStatus.PENDING));
    }

    private void process(List<SearchIndexFailure> tasks) {
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        for (SearchIndexFailure task : tasks) {
            try {
                transaction.executeWithoutResult(status -> searchIndexFailureRepository.findByIdForUpdate(task.getId())
                        .filter(locked -> locked.getStatus() == RetryStatus.PENDING)
                        .ifPresent(this::processLocked));
            } catch (Exception e) {
                // DB 장애/프로세스 종료로 결과가 커밋되지 않으면 PENDING으로 남는다.
                log.error("검색 작업 트랜잭션 실패 - taskId={}", task.getId(), e);
            }
        }
    }

    private void processLocked(SearchIndexFailure task) {
        if (task.getRetryCount() >= MAX_RETRY_COUNT) {
            task.markFailed();
            return;
        }

        try {
            if (task.getResourceType() != ResourceType.EVENT) {
                throw new IllegalArgumentException("지원하지 않는 ResourceType: " + task.getResourceType());
            }
            // ponytail: ES 호출 동안 행 잠금/DB 연결을 유지한다. 처리량 증가 시 lease 기반 점유로 분리한다.
            Event event = eventRepository.findByIdForIndexing(task.getResourceId()).orElse(null);
            if (event == null || event.getStatus() != EventStatus.PUBLISHED) {
                eventIndexerService.delete(task.getResourceId());
            } else {
                eventIndexerService.index(event);
            }
            task.markSuccess();
        } catch (Exception e) {
            log.error("검색 인덱스 재처리 실패 - taskId={}", task.getId(), e);
            task.increaseRetryCount(CommonMapper.normalizeReason(e, "검색 인덱스 재처리 실패"));
            if (task.getRetryCount() >= MAX_RETRY_COUNT) {
                task.markFailed();
            }
        }
    }
}
