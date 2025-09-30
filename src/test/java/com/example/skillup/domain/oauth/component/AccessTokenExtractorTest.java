package com.example.skillup.domain.oauth.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccessTokenExtractorTest {

    private AccessTokenExtractor extractor;

    @BeforeEach
    void setUp() {
        extractor = new AccessTokenExtractor(new ObjectMapper());
    }

    @Test
    void testExtractAccessToken_Success() {
        String json = "{ \"access_token\": \"abc123\"}";

        String accessToken = extractor.extractAccessTokenFromJson(json);

        assertNotNull(accessToken);
        assertEquals("abc123", accessToken);
    }

    @Test
    void testExtractAccessToken_InvalidJson() {
        String invalidJson = "{ access_token: \"abc123\" ";

        String accessToken = extractor.extractAccessTokenFromJson(invalidJson);

        assertNull(accessToken);
    }
}
