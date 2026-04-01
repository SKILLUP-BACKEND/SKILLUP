package com.example.skillup.global.recovery.service;

import com.example.skillup.global.common.CommonMapper;
import com.example.skillup.global.recovery.entity.FileCleanupFailure;
import com.example.skillup.global.recovery.enums.RetryStatus;
import com.example.skillup.global.recovery.repository.FileCleanupFailureRepository;
import com.example.skillup.global.service.S3Service;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FileCleanupRetryService {
    private static final int MAX_RETRY_COUNT = 3;

    private final FileCleanupFailureRepository fileCleanupFailureRepository;
    private final S3Service s3Service;

    @Transactional
    public void retryPendingFailures() {
        List<FileCleanupFailure> failures = fileCleanupFailureRepository.findTop100ByStatusOrderByCreatedAtAsc(
                RetryStatus.PENDING);

        for (FileCleanupFailure failure : failures) {
            if (failure.getRetryCount() >= MAX_RETRY_COUNT) {
                failure.markFailed();
                continue;
            }

            try {

                if (failure.getFileUrl() == null || failure.getFileUrl().isBlank()) {
                    throw new IllegalArgumentException("삭제할 Url이 없습니다.");
                }

                s3Service.deleteFileFromUrl(failure.getFileUrl());
                failure.markSuccess();

            } catch (Exception e) {
                log.error("S3 파일 재처리 실패 - failureId={}", failure.getId(), e);
                String reason = CommonMapper.normalizeReason(e , "S3 파일 재처리 실패");
                failure.increaseRetryCount(reason);
            }
        }
    }
}
