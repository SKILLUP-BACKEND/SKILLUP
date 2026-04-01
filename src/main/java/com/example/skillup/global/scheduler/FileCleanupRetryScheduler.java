package com.example.skillup.global.scheduler;

import com.example.skillup.global.recovery.service.FileCleanupRetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanupRetryScheduler {

    private final FileCleanupRetryService fileCleanupRetryService;

    @Scheduled(cron = "0 0 5 * * *")
    public void retryFileCleanupFailures() {
        log.info("S3 파일 정리 실패 재처리 스케줄러 실행");
        fileCleanupRetryService.retryPendingFailures();
    }
}