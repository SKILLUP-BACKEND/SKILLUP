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
        private String profileImageUrl;
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
    @AllArgsConstructor
    @Builder
    public static class AdminUserResponse
    {
        private Long userId;
        private String name;
        private String email;
        private String createdAt;
        private String socialLoginType;
        private String role;
        private String status;

    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class AdminUserDetailPageResponse
    {
        private Long userId;
        private String name;
        private String email;
        private String createdAt;
        private String socialLoginType;
        private String role;
        private String lastLoginAt;
        private String status;

    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class AdminUserEventActionCountsResponse
    {
        private int viewCount;
        private int saveCount;
        private int applyCount;
    }

    @Builder
    @Getter
    @AllArgsConstructor
    public static class WithDrawReasonCategoryResponse
    {
        private String description;
    }
}
