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
@Table(name = "search_index_failure")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SearchIndexFailure extends RetryFailureBase {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ResourceType resourceType;

    @Column(nullable = false)
    private Long resourceId;

    @Column(nullable = false, length = 100)
    private String indexName;

    @Column(nullable = false, length = 100)
    private String documentId;

    private SearchIndexFailure(
            ResourceType resourceType,
            Long resourceId,
            String indexName,
            String documentId,
            String failureReason
    ) {
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.indexName = indexName;
        this.documentId = documentId;
        initFailure(failureReason);
    }

    public static SearchIndexFailure of(
            ResourceType resourceType,
            Long resourceId,
            String indexName,
            String documentId,
            String failureReason
    ) {
        return new SearchIndexFailure(
                resourceType,
                resourceId,
                indexName,
                documentId,
                failureReason
        );
    }

    // 기존 테이블을 재사용하며, 실패 전 작업도 PENDING으로 기록한다.
    public static SearchIndexFailure pending(Long eventId, String indexName) {
        SearchIndexFailure task = of(ResourceType.EVENT, eventId, indexName, eventId.toString(), "");
        task.initPending();
        return task;
    }
}
