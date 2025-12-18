package com.example.skillup.domain.oauth.Entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SocialLoginType {
    google("구글"),
    kakao("카카오"),
    naver("네이버");

    private final String toKorean;

    }