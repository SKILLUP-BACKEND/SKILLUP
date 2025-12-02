package com.example.skillup.domain.user.dto.response;

import com.example.skillup.domain.event.dto.response.EventResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

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
        List<EventResponse.HomeEventResponse> onGoingEvents;
        List<EventResponse.HomeEventResponse> completedEvents;
        String role;
        int bookmarkCount;
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
    }
}
