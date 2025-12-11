package com.example.skillup.domain.admin.mapper;

import com.example.skillup.domain.admin.dto.AdminResponse;
import com.example.skillup.domain.admin.enums.AdminRole;
import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.Inquiry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AdminMapper {

    public AdminResponse.AdminUserPageResponse toAdminUserPageResponse
            (List<UserResponse.AdminUserResponse> users,
             List<UserResponse.AdminUserResponse> devUsers,
             List<UserResponse.AdminUserResponse> designerUsers,
             List<UserResponse.AdminUserResponse> pmUsers
             ) {
        return AdminResponse.AdminUserPageResponse.builder()
                .users(users)
                .devUsers(devUsers)
                .designerUsers(designerUsers)
                .pmUsers(pmUsers)
                .usersCount(users.size())
                .devUsersCount(devUsers.size())
                .designerUsersCount(designerUsers.size())
                .pmUsersCount(pmUsers.size())
                .build();
    }
}
