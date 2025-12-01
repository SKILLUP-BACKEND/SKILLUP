package com.example.skillup.domain.user.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class UserResponse {
    @Getter
    @AllArgsConstructor
    @Builder
    public static class MyPageHomeResponse {
        private String name;
        private String email;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class MyPageBookMarkResponse {
        private String name;
        private String email;

    }
}
