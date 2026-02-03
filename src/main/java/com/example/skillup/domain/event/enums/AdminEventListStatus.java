package com.example.skillup.domain.event.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminEventListStatus {
    RECRUIT_SCHEDULED("모집예정"),
    RECRUITING("모집중"),
    RECRUIT_CLOSED("모집마감"),
    ENDED("종료");

    private final String toKorean;
}
