package com.example.skillup.domain.user.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class UserRequest
{
    @Getter
    @AllArgsConstructor
    @Builder
    public static class UserUpdateRequest {
        private String name;
        private String profileImageUrl;
        private String age;
        private String gender;
        private String role;
        private List<String> interests;
        private Boolean marketingAgreement;
    }
}
