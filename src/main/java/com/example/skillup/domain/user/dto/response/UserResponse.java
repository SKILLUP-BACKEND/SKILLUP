package com.example.skillup.domain.user.dto.response;

import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.global.common.CommonResponse;
import java.util.List;
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
        private String profileImageUrl;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class MyPageBookMarkResponse {
        private String name;
        private String email;
        String role;
        List<EventResponse.HomeEventResponse> events;
        long closedCount;
        long recruitingCount;
        long bookmarkCount;
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


    @Getter
    @Builder
    @AllArgsConstructor
    public static class RecentSearchItem {
        private Long id;
        private String keyword;
    }

    @Getter
    @Builder
    public static class RecentSearchListResponse {
        private List<RecentSearchItem> items;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class ContinueLoginResponse {
        private String accessToken;
    }
}
