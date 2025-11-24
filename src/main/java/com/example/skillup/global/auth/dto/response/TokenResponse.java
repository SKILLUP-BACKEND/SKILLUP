package com.example.skillup.global.auth.dto.response;

import lombok.Builder;

@Builder
public record TokenResponse(
        String accessToken

) {
    public static TokenResponse of(String accessToken) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .build();
    }
}
