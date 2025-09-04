package com.example.skillup.domain.user.dto.response;

import com.example.skillup.domain.user.entity.Users;

public record UserResponseDto(
        String name
)
{
    public static UserResponseDto from(Users users) {
        return new UserResponseDto(
                users.getName()
        );
    }
}
