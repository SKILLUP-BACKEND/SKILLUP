package com.example.skillup.domain.oauth.mapper;

import com.example.skillup.domain.oauth.dto.OauthResponse;
import com.example.skillup.global.auth.dto.response.TokenResponse;
import org.springframework.stereotype.Component;

@Component
public class OauthMapper
{
    public OauthResponse.OAuthLoginResponse toOauthLoginResponse(
            TokenResponse accessToken,
            String userLoginStatus
    ){
        return OauthResponse.OAuthLoginResponse.builder().accessToken(accessToken).userLoginStatus(userLoginStatus).build();
    }
}
