package com.example.skillup.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserRequest {
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

    @Getter
    @AllArgsConstructor
    @Builder
    public static class UserOAuthSignupRequest {
        private List<String> interests;
        String role;
    }

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserWithdrawRequest {
        @Size(max = 500, message = "탈퇴 사유는 최대 500자까지 입력할 수 있습니다.")
        private String detail;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class SaveRecentSearchRequest {

        @NotBlank(message = "keyword는 공백일 수 없습니다.")
        @Size(max = 200, message = "keyword는 200자를 초과할 수 없습니다.")
        private String keyword;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ContinueLoginRequest {
        private String socialLoginType;
        private String socialId;
    }
}
