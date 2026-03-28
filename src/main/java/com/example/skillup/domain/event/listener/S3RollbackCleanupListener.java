package com.example.skillup.domain.event.listener;

import com.example.skillup.domain.event.events.ThumbnailUploadedEvent;
import com.example.skillup.global.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3RollbackCleanupListener {
    private final S3Service s3Service;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void deleteThumbnailOnRollback(ThumbnailUploadedEvent event) {
        log.info("AFTER_ROLLBACK 실행 - thumbnailUrl={}", event.thumbnailUrl());
        s3Service.deleteFileFromUrl(event.thumbnailUrl());
    }
}
