package com.example.skillup.domain.event.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventStatus {
    DRAFT("임시저장"),
    PUBLISHED("등록");

    private final String toKorean;
}
