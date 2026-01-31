package com.example.skillup.domain.map.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProviderType {
    GOOGLE("구글"),KAKAO("카카오"),NAVER("네이버");

    private final String toKorean;
}
