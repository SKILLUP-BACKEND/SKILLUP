package com.example.skillup.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum JobGroup {

    ALL("IT 전체"), PM("기획자"), DESIGN("디자이너"), DEVELOPMENT("개발자"), AI("AI 개발자");

    private final String toKorean;
}
