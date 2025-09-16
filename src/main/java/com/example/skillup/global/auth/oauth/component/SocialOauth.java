package com.example.skillup.global.auth.oauth.component;

import com.example.skillup.domain.oauth.Entity.SocialLoginType;

public interface SocialOauth {

    String getOauthRedirectURL();


    String requestAccessToken(String code);

    default SocialLoginType type() {
        if (this instanceof GoogleOauth) {
            return SocialLoginType.google;
        } else if (this instanceof NaverOauth) {
            return SocialLoginType.naver;
        } else if (this instanceof KakaoOauth) {
            return SocialLoginType.kakao;
        } else {
            return null;
        }
    }
}