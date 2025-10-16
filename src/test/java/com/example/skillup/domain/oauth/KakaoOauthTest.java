package com.example.skillup.domain.oauth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import com.example.skillup.domain.oauth.component.HttpClientHelper;
import com.example.skillup.domain.oauth.dto.OauthInfo;
import com.example.skillup.global.auth.oauth.component.KakaoOauth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Map;

class KakaoOauthTest {

    private KakaoOauth kakaoOauth;

    @Mock
    private HttpClientHelper httpClientHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        kakaoOauth = new KakaoOauth(httpClientHelper);

        // @Value 필드 설정
        ReflectionTestUtils.setField(kakaoOauth, "KAKAO_SNS_BASE_URL", "https://kauth.kakao.com/oauth/authorize");
        ReflectionTestUtils.setField(kakaoOauth, "KAKAO_SNS_CLIENT_ID", "test-client-id");
        ReflectionTestUtils.setField(kakaoOauth, "KAKAO_SNS_CALLBACK_URL", "https://localhost/callback");
        ReflectionTestUtils.setField(kakaoOauth, "KAKAO_SNS_CLIENT_SECRET", "test-secret");
        ReflectionTestUtils.setField(kakaoOauth, "KAKAO_SNS_TOKEN_BASE_URL", "https://kauth.kakao.com/oauth/token");
    }

    @Test
    void testGetOauthRedirectURL_containsRequiredParams() {
        String redirectUrl = kakaoOauth.getOauthRedirectURL();

        assertTrue(redirectUrl.contains("client_id=test-client-id"));
        assertTrue(redirectUrl.contains("redirect_uri=https://localhost/callback"));
        assertTrue(redirectUrl.contains("response_type=code"));
    }

    @Test
    void testParse_returnsCorrectOauthInfo_fullData() {
        String userInfoJson = """
                {
                  "id": "123456",
                  "kakao_account": {
                    "email": "kakao@example.com",
                    "gender": "female",
                    "age_range": "20~29",
                    "profile": {"nickname": "KakaoUser"}
                  },
                  "properties": {
                    "nickname": "KakaoUserProps"
                  }
                }
                """;

        OauthInfo info = kakaoOauth.parse(userInfoJson, "dummyAccessToken");

        assertEquals("123456", info.socialId());
        assertEquals("KakaoUserProps", info.name()); // properties nickname 우선
        assertEquals("kakao@example.com", info.email());
        assertEquals("female", info.gender());
        assertEquals("20~29", info.age());
    }

    @Test
    void testParse_returnsCorrectOauthInfo_missingProperties() {
        String userInfoJson = """
                {
                  "id": "123456",
                  "kakao_account": {
                    "email": "kakao@example.com"
                  }
                }
                """;

        OauthInfo info = kakaoOauth.parse(userInfoJson, "dummyAccessToken");

        assertEquals("123456", info.socialId());
        assertEquals(null, info.name());
        assertEquals("kakao@example.com", info.email());
        assertNull(info.gender());
        assertNull(info.age());
    }

}
