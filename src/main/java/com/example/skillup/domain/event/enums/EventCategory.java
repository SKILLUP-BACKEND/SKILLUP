package com.example.skillup.domain.event.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventCategory {
    CONFERENCE_SEMINAR("컨퍼런스/세미나"),
    BOOTCAMP_CLUB("부트캠프/동아리"),
    COMPETITION_HACKATHON("공모전/해커톤"),
    ALL("전체"),;

    private final String toKorean;
}
