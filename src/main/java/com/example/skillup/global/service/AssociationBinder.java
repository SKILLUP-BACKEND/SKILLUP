package com.example.skillup.global.service;

import com.example.skillup.domain.event.entity.HashTag;
import com.example.skillup.domain.event.entity.TargetRole;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class AssociationBinder {

    private final NotFoundGuardService notFoundGuardService;

    public void bindRoles(
            Collection<String> roleNames,
            Consumer<TargetRole> consumer
    ) {
        roleNames.stream()
                .distinct()
                .map(notFoundGuardService::getRole)
                .forEach(consumer);
    }

    public void bindHashTags(
            Collection<String> hashTagNames,
            Consumer<HashTag> consumer
    ) {
        hashTagNames.stream()
                .distinct()
                .map(notFoundGuardService::getHashTag)
                .forEach(consumer);
    }
}
