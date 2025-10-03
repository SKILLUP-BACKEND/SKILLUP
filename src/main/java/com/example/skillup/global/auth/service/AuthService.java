package com.example.skillup.global.auth.service;

import com.example.skillup.global.auth.RefreshToken.RefreshToken;
import com.example.skillup.global.auth.RefreshToken.RefreshTokenRepository;
import com.example.skillup.global.auth.dto.response.TokenResponse;
import com.example.skillup.global.auth.jwt.JwtProvider;
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
    public TokenResponse login(Long id, String role) {

        String accessToken = jwtProvider.generateToken(id,role,ACCESS_TOKEN_EXP);
        String refreshToken = jwtProvider.generateToken(id,role,REFRESH_TOKEN_EXP);

        if(id == null){
            refreshTokenRepository.save(RefreshToken.of(refreshToken));
        }
        else {
            refreshTokenRepository.findByUserId(id)
                    .ifPresentOrElse(
                            existing -> refreshTokenRepository.updateTokenByUserId(id, refreshToken),
                            () -> refreshTokenRepository.save(RefreshToken.of(id, refreshToken))
                    );
        }

        return TokenResponse.of(accessToken,refreshToken);
    }
}
