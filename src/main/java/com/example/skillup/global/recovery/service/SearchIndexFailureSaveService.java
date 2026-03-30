package com.example.skillup.global.recovery.service;

import com.example.skillup.global.recovery.entity.SearchIndexFailure;
import com.example.skillup.global.recovery.enums.ResourceType;
import com.example.skillup.global.recovery.repository.SearchIndexFailureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SearchIndexFailureSaveService {

    private final SearchIndexFailureRepository searchIndexFailureRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveFailure(
            ResourceType resourceType,
            Long resourceId,
            String indexName,
            String documentId,
            String failureReason
    ) {
        SearchIndexFailure failure = SearchIndexFailure.of(
                resourceType,
                resourceId,
                indexName,
                documentId,
                failureReason
        );

        searchIndexFailureRepository.save(failure);
    }
}