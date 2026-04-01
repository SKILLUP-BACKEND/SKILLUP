package com.example.skillup.domain.event.listener;

import static com.example.skillup.global.common.CommonMapper.normalizeReason;

import com.example.skillup.domain.event.events.ThumbnailReplacedEvent;
import com.example.skillup.domain.event.events.ThumbnailUploadedEvent;
import com.example.skillup.global.recovery.service.FileCleanupFailureSaveService;
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
    private final FileCleanupFailureSaveService fileCleanupFailureSaveService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_ROLLBACK)
    public void deleteThumbnailOnRollback(ThumbnailUploadedEvent event) {
        try {
            log.info("S3 AFTER_ROLLBACK 실행 - thumbnailUrl={}", event.thumbnailUrl());
            s3Service.deleteFileFromUrl(event.thumbnailUrl());
        } catch (Exception e) {
            log.error("롤백 후 S3 삭제 실패 - thumbnailUrl={}", event.thumbnailUrl(), e);

            fileCleanupFailureSaveService.saveFailure(
                    event.resourceType(),
                    null,
                    event.thumbnailUrl(),
                    null,
                    null,
                    normalizeReason(e, "S3 이미지 롤백 실패")
            );
        }

    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteThumbnailOnCommit(ThumbnailReplacedEvent event) {
        try {
            log.info("S3 AFTER_COMMIT 실행 - thumbnailUrl={}", event.oldThumbnailUrl());
            s3Service.deleteFileFromUrl(event.oldThumbnailUrl());
        } catch (Exception e) {
            log.error("커밋 후 기존 썸네일 삭제 실패 - oldThumbnailUrl={}", event.oldThumbnailUrl(), e);

            fileCleanupFailureSaveService.saveFailure(
                    event.resourceType(),
                    event.resourceId(),
                    event.oldThumbnailUrl(),
                    null,
                    null,
                    normalizeReason(e, "S3 기존 썸네일 삭제 실패")
            );
        }
    }
}
