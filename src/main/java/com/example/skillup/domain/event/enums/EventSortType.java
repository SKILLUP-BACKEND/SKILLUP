package com.example.skillup.domain.event.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventSortType {
    POPULARITY("추천순"),
    LATEST("최신순"),
    DEADLINE("모집 마감일순");

    private final String toKorean;
}
