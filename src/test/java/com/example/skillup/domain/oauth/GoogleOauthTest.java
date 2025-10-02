package com.example.skillup.domain.oauth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


import com.example.skillup.domain.oauth.component.HttpClientHelper;
import com.example.skillup.domain.oauth.dto.OauthInfo;
import com.example.skillup.global.auth.oauth.component.GoogleOauth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;

class GoogleOauthTest {

    private GoogleOauth googleOauth;

    @Mock
    private HttpClientHelper httpClientHelper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        googleOauth = new GoogleOauth(httpClientHelper);

        ReflectionTestUtils.setField(googleOauth, "GOOGLE_SNS_BASE_URL", "https://accounts.google.com/o/oauth2/v2/auth");
        ReflectionTestUtils.setField(googleOauth, "GOOGLE_SNS_CLIENT_ID", "test-client-id");
        ReflectionTestUtils.setField(googleOauth, "GOOGLE_SNS_CALLBACK_URL", "https://localhost/callback");
        ReflectionTestUtils.setField(googleOauth, "GOOGLE_SNS_CLIENT_SECRET", "test-secret");
        ReflectionTestUtils.setField(googleOauth, "GOOGLE_SNS_TOKEN_BASE_URL", "https://oauth2.googleapis.com/token");
    }

    @Test
    void testGetOauthRedirectURL_containsRequiredParams() throws Exception {
        String redirectUrl = googleOauth.getOauthRedirectURL();
        System.out.println(redirectUrl);
        assertTrue(redirectUrl.contains("client_id=test-client-id"));
        assertTrue(redirectUrl.contains("redirect_uri=" + URLEncoder.encode("https://localhost/callback", "UTF-8")));
        assertTrue(redirectUrl.contains("scope=openid+email+profile"));
        assertTrue(redirectUrl.contains("response_type=code"));
    }

    @Test
    void testParse_returnsCorrectOauthInfo() {
        String userInfoJson = """
                {
                  "sub": "1234567890",
                  "name": "John Doe",
                  "email": "john@example.com",
                  "gender": "male",
                  "birthdate": "1990-01-01"
                }
                """;

        OauthInfo oauthInfo = googleOauth.parse(userInfoJson, "dummyAccessToken");

        assertEquals("1234567890", oauthInfo.socialId());
        assertEquals("John Doe", oauthInfo.name());
        assertEquals("john@example.com", oauthInfo.email());
        assertEquals("male", oauthInfo.gender());
        assertEquals("1990-01-01", oauthInfo.age());
    }
}