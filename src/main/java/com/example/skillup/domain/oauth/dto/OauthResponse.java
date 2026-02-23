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
        private String accessToken;
        private String userLoginStatus;
        private OtherOauthUserInfo otherOauthUserInfo;
        private WithdrawPendingUserInfo withdrawPendingUserInfo;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    public static class OtherOauthUserInfo {
        private String socialLoginType;
        private String email;
        private String socialId;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class WithdrawPendingUserInfo {
        private String socialLoginType;
        private String socialId;
    }
}
