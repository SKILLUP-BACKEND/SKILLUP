package com.example.skillup.domain.event.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventSortType {
    POPULARITY("추천순"),
    LATEST("최신순"),
    DEADLINE("모집 마감일순"),

    EVENT_START("행사 시작일 순"),
    VIEWS("조회수 많은 순"),
    BOOKMARKS("저장 많은 순"),
    CREATED_AT("등록일 순");

    private final String toKorean;
}
