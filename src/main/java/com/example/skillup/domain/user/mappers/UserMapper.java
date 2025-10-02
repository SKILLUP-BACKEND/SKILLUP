package com.example.skillup.domain.user.mappers;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.user.dto.response.UserResponseDto;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.enums.UserStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class UserMapper {

    public static Users of(String email, String name, String socialId, SocialLoginType socialLoginType, String gender, String age) {
        return Users.builder()
                .email(email)
                .name(name)
                .regDatetime(LocalDateTime.now())
                .role("USER")
                .status(UserStatus.ACTIVE)
                .jobGroup("UNKNOWN")
                .notificationFlag("Y")
                .lastLoginAt(LocalDateTime.now())
                .socialId(socialId)
                .socialLoginType(socialLoginType)
                .gender(gender)
                .age(age)
                .build();
    }

    public static UserResponseDto from(Users users) {
        return new UserResponseDto(
                users.getName()
        );
    }
}
