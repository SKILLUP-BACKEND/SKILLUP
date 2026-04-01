package com.example.skillup.global.recovery.entity;

import com.example.skillup.global.recovery.enums.ResourceType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "file_cleanup_failure")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileCleanupFailure extends RetryFailureBase {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResourceType resourceType;

    @Column
    private Long resourceId; // 해당 DB 에서 저장되어있는 ID

    @Column(length = 500)
    private String fileUrl;

    @Column(length = 255)
    private String bucket;

    @Column(length = 500)
    private String objectKey;

    private FileCleanupFailure(
            ResourceType resourceType,
            Long resourceId,
            String fileUrl,
            String bucket,
            String objectKey,
            String failureReason
    ) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.fileUrl = fileUrl;
        this.bucket = bucket;
        this.objectKey = objectKey;
        initFailure(failureReason);
    }

    public static FileCleanupFailure of(
            ResourceType resourceType,
            Long resourceId,
            String fileUrl,
            String bucket,
            String objectKey,
            String failureReason
    ) {
        return new FileCleanupFailure(
                resourceType,
                resourceId,
                fileUrl,
                bucket,
                objectKey,
                failureReason
        );
    }
}