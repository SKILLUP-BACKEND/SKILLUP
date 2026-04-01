package com.example.skillup.global.common;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.data.domain.Pageable;

public class CommonMapper {

    private static final int MAX_REASON_LENGTH = 1000;

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

    public static CommonResponse.PageInfoResponse toPageInfoResponse
            (Pageable pageable, int page, int count) {
        return CommonResponse.PageInfoResponse
                .builder()
                .currentPage(page + 1)
                .pageSize(pageable.getPageSize())
                .totalPages((int) Math.ceil((double) count / (pageable.getPageSize())))
                .build();
    }

    public static BigDecimal toBigDecimal(Double value) {
        if (value == null) {
            return null;
        }
        return BigDecimal.valueOf(value).setScale(7, RoundingMode.HALF_UP);
    }

    public static String normalizeReason(Exception e, String prefixMessage) {

        String result;

        if (e == null || e.getMessage() == null || e.getMessage().isBlank()) {
            result = prefixMessage + ": 알 수 없는 오류";
        } else if (prefixMessage == null || prefixMessage.isBlank()) {
            result = e.getMessage();
        } else {
            result = prefixMessage + ": " + e.getMessage();
        }

        return result.length() > MAX_REASON_LENGTH
                ? result.substring(0, MAX_REASON_LENGTH)
                : result;
    }
}
