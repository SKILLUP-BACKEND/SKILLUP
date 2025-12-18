package com.example.skillup.domain.article.mapper;

import static java.util.stream.Collectors.toSet;

import com.example.skillup.domain.article.dto.request.ArticleRequest;
import com.example.skillup.domain.article.dto.response.ArticleResponse.AdminArticleDetailsResponse;
import com.example.skillup.domain.article.dto.response.ArticleResponse.AdminArticleResponse;
import com.example.skillup.domain.article.dto.response.ArticleResponse.AdminArticleResponseList;
import com.example.skillup.domain.article.dto.response.ArticleResponse.HomeArticleResponse;
import com.example.skillup.domain.article.dto.response.ArticleResponse.HomeArticleResponseList;
import com.example.skillup.domain.article.entity.Article;
import com.example.skillup.domain.event.entity.TargetRole;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ArticleMapper {

    public Article toArticleEntity(ArticleRequest.CreateArticleRequest createArticleRequest, String thumbnailUrl) {
        return Article.builder()
                .title(createArticleRequest.getTitle())
                .summary(createArticleRequest.getSummary())
                .originalUrl(createArticleRequest.getOriginalUrl())
                .originalPublishedDate(createArticleRequest.getOriginalPublishedDate())
                .source(createArticleRequest.getSource())
                .status(createArticleRequest.getStatus())
                .thumbnailUrl(thumbnailUrl)
                .build();
    }

    public AdminArticleResponse toAdminArticleResponse(Article article) {
        return AdminArticleResponse.builder()
                .thumbnailUrl(article.getThumbnailUrl())
                .title(article.getTitle())
                .source(article.getSource())
                .originalPublishedDate(article.getOriginalPublishedDate())
                .targetRoles(article.getTargetRoles().stream().map(TargetRole::getName).collect(toSet()))
                .status(article.getStatus())
                .createdAt(article.getCreatedAt())
                .build();
    }

    public AdminArticleResponseList toAdminArticleResponseList(List<AdminArticleResponse> articles, Long publishedTotal,
                                                               Long draftTotal, Long totalPages, String sortType) {
        return AdminArticleResponseList.builder()
                .publishedTotal(publishedTotal)
                .draftTotal(draftTotal)
                .totalPages(totalPages)
                .articles(articles)
                .sortType(sortType)
                .build();
    }

    public AdminArticleDetailsResponse toAdminArticleDetailsResponse(Article article) {
        return AdminArticleDetailsResponse.builder()
                .title(article.getTitle())
                .summary(article.getSummary())
                .source(article.getSource())
                .originalPublishedDate(article.getOriginalPublishedDate())
                .targetRoles(article.getTargetRoles().stream().map(TargetRole::getName).collect(Collectors.toSet()))
                .createdAt(article.getCreatedAt())
                .originalUrl(article.getOriginalUrl())
                .clickCount(article.getClickCount())
                .thumbnailUrl(article.getThumbnailUrl())
                .build();
    }

    public HomeArticleResponse toHomeArticleResponse(Article article) {
        return HomeArticleResponse.builder()
                .title(article.getTitle())
                .summary(article.getSummary())
                .source(article.getSource())
                .originalPublishedDate(article.getOriginalPublishedDate())
                .targetRoles(article.getTargetRoles().stream().map(TargetRole::getName).collect(Collectors.toSet()))
                .thumbnailUrl(article.getThumbnailUrl())
                .originalUrl(article.getOriginalUrl())
                .build();
    }

    public HomeArticleResponseList toHomeArticleResponseList(List<HomeArticleResponse> articles, Long totalArticles,
                                                             Long totalPages) {
        return HomeArticleResponseList.builder()
                .articles(articles)
                .totalArticles(totalArticles)
                .totalPages(totalPages)
                .build();
    }
}
