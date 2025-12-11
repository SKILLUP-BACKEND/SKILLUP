package com.example.skillup.global.common;

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
}
