package com.example.skillup.global.recovery.entity;

import com.example.skillup.global.recovery.enums.RetryStatus;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@MappedSuperclass
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class RetryFailureBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RetryStatus status = RetryStatus.PENDING;

    @Column(nullable = false)
    private int retryCount = 0;

    @Column(nullable = false, length = 1000)
    private String failureReason;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime lastTriedAt;

    private LocalDateTime processedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    protected void initFailure(String failureReason) {
        this.status = RetryStatus.PENDING;
        this.retryCount = 1;
        this.failureReason = failureReason;
        this.lastTriedAt = LocalDateTime.now();
    }

    public void markSuccess() {
        this.status = RetryStatus.SUCCEEDED;
        this.processedAt = LocalDateTime.now();
    }

    public void increaseRetryCount(String failureReason) {
        this.retryCount++;
        this.failureReason = failureReason;
        this.lastTriedAt = LocalDateTime.now();
    }

    public void markFailed() {
        this.status = RetryStatus.FAILED;
        this.lastTriedAt = LocalDateTime.now();
    }
}