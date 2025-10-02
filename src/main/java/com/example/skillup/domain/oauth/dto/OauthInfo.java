package com.example.skillup.domain.oauth.dto;

import com.example.skillup.domain.oauth.Entity.SocialLoginType;

public record OauthInfo(
        String email,
        String name,
        String age,
        String gender,
        String socialId,
        SocialLoginType socialLoginType
) {
    public static OauthInfo of(String email, String name, String socialId,
                               SocialLoginType socialLoginType, String gender, String age) {
        return new OauthInfo(email, name, age, gender, socialId, socialLoginType);
    }
}
