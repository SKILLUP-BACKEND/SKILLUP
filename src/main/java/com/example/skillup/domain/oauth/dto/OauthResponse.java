package com.example.skillup.domain.oauth.dto;

import com.example.skillup.global.auth.dto.response.TokenResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class OauthResponse {
    @Getter
    @AllArgsConstructor
    @Builder
    public static class OAuthLoginResponse {
        private TokenResponse accessToken;
        private String userLoginStatus;
    }
}
