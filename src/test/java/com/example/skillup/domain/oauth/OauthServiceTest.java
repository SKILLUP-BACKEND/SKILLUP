package com.example.skillup.domain.oauth;

import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.oauth.component.AccessTokenExtractor;
import com.example.skillup.domain.oauth.component.OauthClientFactory;
import com.example.skillup.domain.oauth.dto.OauthRequest;
import com.example.skillup.domain.oauth.dto.OauthResponse;
import com.example.skillup.domain.oauth.service.OauthService;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.auth.oauth.component.SocialOauth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
@ActiveProfiles("test")
@SpringBootTest
class OauthServiceTest {

    @Autowired
    private OauthService oauthService;

    @MockitoBean
    private AccessTokenExtractor accessTokenExtractor;

    @MockitoBean
    private OauthClientFactory oauthClientFactory;
    // ... 다른 의존성 Mock들 (oauthClientFactory, userMapper 등)

    @Mock
    private SocialOauth socialOauth;
    @Test
    @DisplayName("이미 다른 소셜로 가입된 이메일인 경우 OTHER_OAUTH_USER 상태를 반환한다")
    void otherOauthUserTest() {
        // Given

        String email = "lemonherb0323@naver.com";
        String socialId = "l6-0CWDu9goKezN4IQHyebFPxAYbMl80RU-tjbPn0Mc";

        // 1. 외부 API 호출 관련 Mock 설정 (전부 통과)
        when(oauthClientFactory.getClient(SocialLoginType.naver)).thenReturn(socialOauth);
        when(socialOauth.requestAccessToken(anyString())).thenReturn("mock_json");
        when(accessTokenExtractor.extractAccessTokenFromJson(anyString())).thenReturn("mock_token");
        when(socialOauth.getUserInfo(anyString())).thenReturn("mock_user_info");

        OauthRequest mockOauthInfo = OauthRequest.of(email,"userName",socialId,SocialLoginType.naver,"N","34");
        when(socialOauth.parse(anyString(), anyString())).thenReturn(mockOauthInfo);


        // When
        OauthResponse.OAuthLoginResponse response = oauthService.requestAccessTokenAndSaveUser(SocialLoginType.naver, "code");


    }}
