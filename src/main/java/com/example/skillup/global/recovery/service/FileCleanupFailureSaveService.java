package com.example.skillup.global.recovery.service;

import com.example.skillup.global.recovery.entity.FileCleanupFailure;
import com.example.skillup.global.recovery.enums.ResourceType;
import com.example.skillup.global.recovery.repository.FileCleanupFailureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FileCleanupFailureSaveService {
    private final FileCleanupFailureRepository fileCleanupFailureRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailure(ResourceType resourceType,
                            Long resourceId,
                            String fileUrl,
                            String bucket,
                            String objectKey,
                            String failureReason
    ) {
        FileCleanupFailure failure = FileCleanupFailure.of(
                resourceType,
                resourceId,
                fileUrl,
                bucket,
                objectKey,
                failureReason
        );
        fileCleanupFailureRepository.save(failure);
    }
}
