package com.example.skillup.domain.article.dto.response;

import com.example.skillup.domain.article.enums.ArticleStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class ArticleResponse {

    @AllArgsConstructor
    @Getter
    public static class CommonArticleResponse {
        private Long id;
    }

    @AllArgsConstructor
    @Getter
    @Builder
    public static class AdminArticleResponseList {
        private Long publishedTotal;
        private Long draftTotal;
        private Long totalPages;
        private String sortType;
        private List<AdminArticleResponse> articles;
    }

    @AllArgsConstructor
    @Getter
    @Builder
    public static class AdminArticleResponse {
        private String thumbnailUrl;
        private String title;
        private String source;
        private LocalDate originalPublishedDate;
        private Set<String> targetRoles;
        private ArticleStatus status;
        private LocalDateTime createdAt;
    }

    @AllArgsConstructor
    @Getter
    @Builder
    public static class AdminArticleDetailsResponse {
        private String title;
        private String summary;
        private String source;
        private LocalDate originalPublishedDate;
        private Set<String> targetRoles;
        private LocalDateTime createdAt;
        private String originalUrl;
        private Long clickCount;
        private String thumbnailUrl;
    }

    @AllArgsConstructor
    @Getter
    @Builder
    public static class HomeArticleResponseList {
        private Long totalArticles;
        private Long totalPages;
        private List<HomeArticleResponse> articles;
    }

    @AllArgsConstructor
    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class HomeArticleResponse {
        private String title;
        private String summary;
        private String source;
        private LocalDate originalPublishedDate;
        private String originalUrl;
        private Set<String> targetRoles;
        private String thumbnailUrl;
    }
}
