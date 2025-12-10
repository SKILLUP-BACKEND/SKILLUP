package com.example.skillup.domain.user.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


public class UserRequest
{
    @Getter
    @AllArgsConstructor
    @Builder
    public static class UserUpdateRequest {
        private String name;
        private String age;
        private String gender;
        private String role;
        private List<String> interests;
        private Boolean marketingAgreement;
    }
}
