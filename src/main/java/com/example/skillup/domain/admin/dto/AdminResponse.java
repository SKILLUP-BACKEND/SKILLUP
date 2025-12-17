package com.example.skillup.domain.admin.dto;

import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.global.common.CommonResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class AdminResponse {
    @Getter
    @AllArgsConstructor
    @Builder
    public static class AdminUserPageResponse
    {
        List<UserResponse.AdminUserResponse> users;
        List<UserResponse.AdminUserResponse> devUsers ;
        List<UserResponse.AdminUserResponse> designerUsers;
        List<UserResponse.AdminUserResponse> pmUsers;

        CommonResponse.PageInfoResponse pageInfoResponse;
        int usersCount;
        int devUsersCount;
        int designerUsersCount;
        int pmUsersCount;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class eventActionMonthlyCountResponse
    {
        int userMonthlyCount;
        int othersMonthlyCount;
        String monthLabels;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class  rolePercentageResponse
    {
        String role;
        int percentage;
    }



    @Getter
    @AllArgsConstructor
    @Builder
    public static class eventActionAnalyticsResponse
    {
        List<eventActionMonthlyCountResponse> eventActionMonthlyCountResponses;
        List<rolePercentageResponse> rolePercentageResponses;
    }


}
