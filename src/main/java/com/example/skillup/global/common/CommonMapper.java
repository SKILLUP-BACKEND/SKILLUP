package com.example.skillup.global.common;

import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.Users;
import org.springframework.data.domain.Pageable;

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

    public static CommonResponse.PageInfoResponse toPageInfoResponse
            (Pageable pageable, int page,int count)
    {
        return CommonResponse.PageInfoResponse
                .builder()
                .currentPage(page+1)
                .pageSize(pageable.getPageSize())
                .totalPages((int) Math.ceil((double) count / (pageable.getPageSize())))
                .build();
    }
}
