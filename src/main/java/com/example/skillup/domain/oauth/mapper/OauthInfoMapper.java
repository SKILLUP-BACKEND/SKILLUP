package com.example.skillup.domain.oauth.mapper;

import com.example.skillup.domain.oauth.Entity.OauthInfo;
import com.example.skillup.domain.oauth.Entity.SocialLoginType;

public class OauthInfoMapper {
    public static OauthInfo of(String email, String name, String socialId, SocialLoginType socialLoginType, String gender, String age) {
        return OauthInfo.builder()
                .email(email)
                .name(name)
                .socialId(socialId)
                .socialLoginType(socialLoginType)
                .gender(gender)
                .age(age)
                .build();
    }
}
