package com.example.skillup.global.auth.oauth.component;

import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.oauth.dto.OauthInfo;

public interface SocialOauth {

    String getOauthRedirectURL();

    String getUserInfo(String accessToken);

    String requestAccessToken(String code);

    OauthInfo parse(String userInfo, String accessToken);

    default SocialLoginType getSocialType () {
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