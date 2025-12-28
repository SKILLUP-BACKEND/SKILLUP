package com.example.skillup.global.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.data.domain.Pageable;

public class CommonMapper {
    public static String toDatePattern(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm");
        return dateTime.format(formatter);
    }

    public static String convertRole(String role) {
        return switch (role) {
            case "개발자" -> "개발";
            case "디자이너" -> "디자인";
            default -> "기획";
        };
    }

    public static String resolveRoleName(String tab) {
        if (tab == null || tab.isBlank() || "IT 전체".equals(tab) || tab.equals("전체")) {
            return null;
        }
        return switch (tab) {
            case "기획" -> "기획자";
            case "디자인" -> "디자이너";
            case "개발" -> "개발자";
            case "AI" -> "AI 개발자";
            default -> null;
        };
    }

    public static CommonResponse.PageInfoResponse toPageInfoResponse
            (Pageable pageable, int page, int count) {
        return CommonResponse.PageInfoResponse
                .builder()
                .currentPage(page + 1)
                .pageSize(pageable.getPageSize())
                .totalPages((int) Math.ceil((double) count / (pageable.getPageSize())))
                .build();
    }
}
