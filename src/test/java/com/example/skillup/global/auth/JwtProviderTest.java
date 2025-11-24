package com.example.skillup.global.auth;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;

import com.example.skillup.global.auth.jwt.JwtProperties;
import com.example.skillup.global.auth.jwt.JwtProvider;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetailsService;

public class JwtProviderTest {

    private final JwtProperties jwtProperties = new JwtProperties("my-secret-key-my-secret-key-my-secret-key", "U3VwZXJTZWNyZXRLZXlTdHJpbmdGb3JKV1QxMjM0NTY=");
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final JwtProvider jwtProvider = new JwtProvider(jwtProperties , userDetailsService);

    @Test
    void testGenerateAndValidateToken() {
        // given
        String email = "VIEWER";
        String role = "VIEWER";


        // when
        String token = jwtProvider.generateToken(email, role, Duration.ofHours(1));

        // then
        System.out.println("토큰: "+token);
        assertThat(token).isNotBlank();
        assertThat(jwtProvider.validateToken(token)).isTrue();
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
