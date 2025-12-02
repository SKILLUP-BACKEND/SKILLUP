package com.example.skillup.global.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class CommonResponse {
    @Getter
    @Builder
    @AllArgsConstructor
    public static class PageInfoResponse {
        private int currentPage;
        private int pageSize;
        private int totalPages;
    }
}
