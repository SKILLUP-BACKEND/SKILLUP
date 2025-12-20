package com.example.skillup.domain.article.dto.request;

import com.example.skillup.domain.article.enums.ArticleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class ArticleRequest {

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CreateArticleRequest {

        @NotBlank(message = "제목을 입력해주세요")
        private String title;

        @NotBlank(message = "아티클 내용 요약을 입력해주세요")
        private String summary;

        @NotBlank(message = "원본 링크를 입력해주세요")
        private String originalUrl;

        private ArticleStatus status = ArticleStatus.PUBLISHED;

        @NotNull(message = "출처를 적어주세요")
        private String source;

        @NotNull(message = "원본의 게시글 시작일을 입력해주세요")
        private LocalDate originalPublishedDate;

        @NotNull(message = "추천 대상 최소 1개 선택해주세요.")
        @Size(min = 1, message = "최소 1개의 추천 대상이 필요합니다.")
        //"개발자", "AI" 등등
        private List<String> targetRoles;
    }


    @Getter
    @NoArgsConstructor
    public static class AdminUpdateArticleRequest {
        @NotBlank(message = "제목을 입력해주세요")
        private String title;

        @NotBlank(message = "아티클 내용 요약을 입력해주세요")
        private String summary;

        @NotBlank(message = "원본 링크를 입력해주세요")
        private String originalUrl;

        @NotNull(message = "아티클의 상태값을 선택해주세요 PUBLISHED / DRAFT")
        private ArticleStatus status;

        @NotNull(message = "출처를 적어주세요")
        private String source;

        @NotNull(message = "원본의 게시글 시작일을 입력해주세요")
        private LocalDate originalPublishedDate;

        @NotNull(message = "추천 대상 최소 1개 선택해주세요.")
        @Size(min = 1, message = "최소 1개의 추천 대상이 필요합니다.")
        //"개발자", "AI" 등등
        private List<String> targetRoles;
    }
}
