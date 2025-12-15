package com.example.skillup.domain.user.dto.response;

import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.global.common.CommonMapper;
import com.example.skillup.global.common.CommonResponse;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

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
        List<EventResponse.HomeEventResponse> recruitingEvents;
        List<EventResponse.HomeEventResponse> closedEvents;
        String role;
        int bookmarkCount;
        CommonResponse.PageInfoResponse pageInfo;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class InterestResponse {
     private String name;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class UserProfileResponse {
        private String name;
        private String profileImageUrl;
        private String age;
        private String gender;
        private String role;
        private List<String> interests;
        private boolean marketingAgreement;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class InquiryResponse
    {
        private String question;
        private String answerTitle;
        private String answerContent;
    }

    @Getter
    public static class AdminUserResponse
    {
        private Long userId;
        private String name;
        private String email;
        private String createdAt;
        private String socialLoginType;
        private String role;
        private String status;

        public AdminUserResponse(Long userId,String name,
                                     String email,
                                     LocalDateTime createdAt,
                                     SocialLoginType socialLoginType,
                                     TargetRole role,
                                     boolean isDeleted) {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.createdAt = CommonMapper.toDatePattern(createdAt);
            this.socialLoginType = socialLoginType.getToKorean();
            this.role = CommonMapper.convertRole(role);
            this.status = isDeleted ? "탈퇴" : "활성";
        }
    }

    @Getter
    public static class AdminUserDetailPageResponse
    {
        private Long userId;
        private String name;
        private String email;
        private String createdAt;
        private String socialLoginType;
        private String role;
        private String lastLoginAt;

        public AdminUserDetailPageResponse(Long userId, String name, String email,
                                           LocalDateTime createdAt, SocialLoginType socialLoginType,
                                           TargetRole role, LocalDateTime lastLoginAt)
        {
            this.userId = userId;
            this.name = name;
            this.email = email;
            this.createdAt = CommonMapper.toDatePattern(createdAt);
            this.socialLoginType = socialLoginType.getToKorean()+" 로그인";
            this.role = CommonMapper.convertRole(role);
            this.lastLoginAt = CommonMapper.toDatePattern(lastLoginAt);
        }
    }
}
