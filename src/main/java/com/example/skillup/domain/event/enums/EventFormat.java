package com.example.skillup.domain.event.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EventFormat {
    ONLINE("온라인"),
    OFFLINE("오프라인"),
    HYBRID("모두");

    private final String toKorean;
}

