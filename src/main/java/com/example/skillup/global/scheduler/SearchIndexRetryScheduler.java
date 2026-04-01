package com.example.skillup.global.scheduler;

import com.example.skillup.global.recovery.service.SearchIndexRetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SearchIndexRetryScheduler {

    private final SearchIndexRetryService searchIndexRetryService;

    @Scheduled(fixedDelay = 900000) // 15분 주기
    public void retrySearchIndexFailures() {
        log.info("검색 인덱스 실패 재처리 스케줄러 실행");
        searchIndexRetryService.retryPendingFailures();
    }
}