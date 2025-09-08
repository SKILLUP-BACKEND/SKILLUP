package com.example.skillup.domain.event.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventCategory {
    CONFERENCE_SEMINAR("컨퍼런스/세미나"),
    COMPETITION_HACKATHON("공모전/해커톤"),
    BOOTCAMP_CLUB("부트캠프/동아리"),
    NETWORKING_MENTORING("네트워킹/멘토링");

    private final String toKorean;
}
