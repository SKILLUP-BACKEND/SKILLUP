package com.example.skillup.global.auth;

import com.example.skillup.global.auth.jwt.JwtProperties;
import com.example.skillup.global.auth.jwt.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class JwtProviderTest {

    private final JwtProperties jwtProperties = new JwtProperties("my-secret-key-my-secret-key-my-secret-key", "U3VwZXJTZWNyZXRLZXlTdHJpbmdGb3JKV1QxMjM0NTY=");
    private final JwtProvider jwtProvider = new JwtProvider(jwtProperties);

    @Test
    void testGenerateAndValidateToken() {
        // given
        Long userId = 123L;
        String role = "VIEWER";


        // when
        String token = jwtProvider.generateToken(userId, role, Duration.ofHours(1));

        // then
        System.out.println("토큰: "+token);
        assertThat(token).isNotBlank();
        assertThat(jwtProvider.validateToken(token)).isTrue();
    }

    @Test
    void testGetAuthentication() {
        // given
        Long userId = 123L;
        String role = "VIEWER";
        String token = jwtProvider.generateToken(userId, role,Duration.ofHours(1));

        // when
        Authentication authentication = jwtProvider.getAuthentication(token);

        // then
        assertThat(authentication).isNotNull();
        assertThat(authentication.getName()).isEqualTo("123");
        System.out.println(authentication);
    }

    @Test
    void testInvalidToken() {
        // given
        String invalidToken = "this.is.invalid.token";

        // when
        boolean result = jwtProvider.validateToken(invalidToken);

        // then
        assertThat(result).isFalse();
    }
}
