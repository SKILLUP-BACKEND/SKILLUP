package com.example.skillup.domain.oauth.mapper;

import com.example.skillup.domain.oauth.dto.OauthResponse;
import com.example.skillup.global.auth.dto.response.TokenResponse;
import org.springframework.stereotype.Component;

@Component
public class OauthMapper
{
    public OauthResponse.OAuthLoginResponse toOauthLoginResponse(
            String accessToken,
            String userLoginStatus
    ){
        return OauthResponse.OAuthLoginResponse.builder().accessToken(accessToken).userLoginStatus(userLoginStatus).build();
    }

    public OauthResponse.OAuthLoginResponse toOtherOauthUserResponse(
            String socialLoginType,
            String email,
            String socialId,
            String userLoginStatus

    ) {
        OauthResponse.OtherOauthUserInfo info =
                OauthResponse.OtherOauthUserInfo.builder()
                        .socialLoginType(socialLoginType)
                        .email(email)
                        .socialId(socialId)
                        .build();

        return OauthResponse.OAuthLoginResponse.builder()
                .userLoginStatus(userLoginStatus)
                .otherOauthUserInfo(info)
                .build();
    }

    public OauthResponse.OAuthLoginResponse toWithdrawPendingUserResponse(
            String socialLoginType,
            String socialId,
            String userLoginStatus
    ) {
        OauthResponse.WithdrawPendingUserInfo info =
                OauthResponse.WithdrawPendingUserInfo.builder()
                        .socialLoginType(socialLoginType)
                        .socialId(socialId)
                        .build();

        return OauthResponse.OAuthLoginResponse.builder()
                .userLoginStatus(userLoginStatus)
                .withdrawPendingUserInfo(info)
                .build();
    }
}
