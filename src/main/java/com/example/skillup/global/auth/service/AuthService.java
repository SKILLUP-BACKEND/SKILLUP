package com.example.skillup.global.auth.service;

import com.example.skillup.global.auth.RefreshToken.RefreshToken;
import com.example.skillup.global.auth.RefreshToken.RefreshTokenRepository;
import com.example.skillup.global.auth.dto.response.TokenResponse;
import com.example.skillup.global.auth.jwt.JwtProvider;
import com.example.skillup.global.exception.CommonErrorCode;
import com.example.skillup.global.exception.GlobalException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService
{
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private static final Duration REFRESH_TOKEN_EXP = Duration.ofDays(1);
    private static final Duration ACCESS_TOKEN_EXP = Duration.ofHours(1);

    @Transactional
    public TokenResponse login(String email, String role) {

        String accessToken = jwtProvider.generateToken(email,role,ACCESS_TOKEN_EXP);
        String refreshToken = jwtProvider.generateToken(email,role,REFRESH_TOKEN_EXP);

            refreshTokenRepository.findByEmail(email)
                    .ifPresentOrElse(
                            exiting -> refreshTokenRepository.updateTokenByUserId(email, refreshToken),
                            () -> refreshTokenRepository.save(RefreshToken.builder().email(email).refreshToken(refreshToken).build())
                    );


        return TokenResponse.of(accessToken);
    }

    public RefreshToken getRefreshTokenByUserId(String email) {
        return refreshTokenRepository.findByEmail(email)
                .orElseThrow(() -> new GlobalException(CommonErrorCode.DATA_NOT_FOUND));
    }
}
