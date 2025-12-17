package com.example.skillup.global.common;

import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.Users;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CommonMapper {
    public static String toDatePattern(LocalDateTime dateTime)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        return dateTime.format(formatter);
    }

    public static String convertRole(String role) {
        return switch (role) {
            case "개발자" -> "개발";
            case "디자이너" -> "디자인";
            default ->  "기획";
        };
    }
}
