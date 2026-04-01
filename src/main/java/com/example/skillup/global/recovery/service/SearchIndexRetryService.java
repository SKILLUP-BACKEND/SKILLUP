package com.example.skillup.global.recovery.service;

import com.example.skillup.domain.event.entity.Event;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchIndexRetryService {
    private static final int MAX_RETRY_COUNT = 3;

    private final SearchIndexFailureRepository searchIndexFailureRepository;
    private final EventRepository eventRepository;
    private final EventIndexerService eventIndexerService;

    @Transactional
    public void retryPendingFailures() {
        List<SearchIndexFailure> failures =
                searchIndexFailureRepository.findTop100ByStatusOrderByCreatedAtAsc(RetryStatus.PENDING);

        for (SearchIndexFailure failure : failures) {
            if (failure.getRetryCount() >= MAX_RETRY_COUNT) {
                failure.markFailed();
                continue;
            }

            try {
                retry(failure);
                failure.markSuccess();

            } catch (Exception e) {
                log.error("검색 인덱스 재처리 실패 - failureId={}", failure.getId(), e);

                String reason = CommonMapper.normalizeReason(e, "검색 인덱스 재처리 실패");
                failure.increaseRetryCount(reason);

                if (failure.getRetryCount() >= MAX_RETRY_COUNT) {
                    failure.markFailed();
                }
            }
        }
    }

    private void retry(SearchIndexFailure failure) {
        if (failure.getResourceType() == ResourceType.EVENT) {
            Event event = eventRepository.getEvent(failure.getResourceId());
            eventIndexerService.index(event);
            return;
        }

        throw new IllegalArgumentException("지원하지 않는 ResourceType 입니다. resourceType=" + failure.getResourceType());
    }
}
