package com.example.skillup.domain.admin.dto;

import com.example.skillup.domain.user.dto.response.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.ArrayList;
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

        int usersCount;
        int devUsersCount;
        int designerUsersCount;
        int pmUsersCount;
    }

}
