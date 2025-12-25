package com.example.skillup.domain.article.service;

import com.example.skillup.domain.article.dto.request.ArticleRequest;
import com.example.skillup.domain.article.dto.response.ArticleResponse;
import com.example.skillup.domain.article.dto.response.ArticleResponse.CommonArticleResponse;
import com.example.skillup.domain.article.entity.Article;
import com.example.skillup.domain.article.enums.ArticleStatus;
import com.example.skillup.domain.article.exception.ArticleErrorCode;
import com.example.skillup.domain.article.exception.ArticleException;
import com.example.skillup.domain.article.mapper.ArticleMapper;
import com.example.skillup.domain.article.repository.ArticleRepository;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.exception.TargetRoleErrorCode;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.global.aop.ConvertNotFound;
import com.example.skillup.global.service.NotFoundGuardService;
import com.example.skillup.global.service.S3Service;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ArticleService {
    private final ArticleRepository articleRepository;
    private final ArticleMapper articleMapper;
    private final S3Service s3Service;
    private final NotFoundGuardService notFoundGuardService;

    private static final Set<String> ALLOWED_SORTS = Set.of("게시일순", "등록일순");



    @Transactional
    public Article createArticle(ArticleRequest.CreateArticleRequest request, MultipartFile thumbnailImage) {

        String thumbnailUrl = s3Service.uploadFile(thumbnailImage, "article/thumbnail");

        Article article = articleMapper.toArticleEntity(request, thumbnailUrl);

        request.getTargetRoles().stream()
                .distinct()
                .forEach(roleName -> {
                    TargetRole role = notFoundGuardService.getRole(roleName);
                    article.addTargetRole(role);
                });

        Article savedArticle = articleRepository.save(article);

        //일라스틱 검색 기능은 추후 일라스틱 서치 수정할때 고민해보겠습니다!

        return savedArticle;
    }

    @Transactional(readOnly = true)
    public ArticleResponse.AdminArticleResponseList getAdminArticle(ArticleStatus articleStatus, int page,
                                                                    String keyword, String sort) {

        Sort sortType = resolveSort(sort);
        Pageable pageable = PageRequest.of(page, 10, sortType);

        Page<Article> publishedResultPage = articleRepository.findByStatusAndKeyword(ArticleStatus.PUBLISHED, keyword,
                pageable);
        Page<Article> draftResultPage = articleRepository.findByStatusAndKeyword(ArticleStatus.DRAFT, keyword,
                pageable);

        return articleMapper.toAdminArticleResponseList(publishedResultPage, draftResultPage, articleStatus, sort);

    }

    private Sort resolveSort(String sort) {
        String key = (sort == null || sort.isBlank()) ? "게시일순" : sort;

        if (!ALLOWED_SORTS.contains(key)) {
            throw new ArticleException(ArticleErrorCode.INVALID_ARTICLE_SORT_TYPE, "sort 는 게시일순 , 등록일순만 가능합니다.");
        }

        key = key.equals("게시일순") ? "originalPublishedDate" : "createdAt";

        return Sort.by(Direction.DESC, key);
    }

    @Transactional(readOnly = true)
    public ArticleResponse.AdminArticleDetailsResponse getAdminArticleDetail(Long articleId) {
        Article article = articleRepository.getArticle(articleId);
        return articleMapper.toAdminArticleDetailsResponse(article);
    }

    @Transactional
    public CommonArticleResponse deleteArticle(Long articleId) {
        Article article = articleRepository.getArticle(articleId);

        article.delete();

        return new CommonArticleResponse(article.getId());
    }

    @Transactional
    public ArticleResponse.CommonArticleResponse updateAdminArticle(Long articleId,
                                                                    ArticleRequest.AdminUpdateArticleRequest request,
                                                                    MultipartFile thumbnailImage) {
        Article article = articleRepository.getArticle(articleId);

        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            String thumbnailUrl = s3Service.uploadFile(thumbnailImage, "article/thumbnail");
            article.setThumbnailUrl(thumbnailUrl);
        }

        article.updateInfo(request);

        if (request.getTargetRoles() != null && !request.getTargetRoles().isEmpty()) {
            article.getTargetRoles().clear();

            request.getTargetRoles().stream().distinct().forEach(name -> {
                TargetRole role = notFoundGuardService.getRole(name);
                article.addTargetRole(role);
            });
        }

        return new CommonArticleResponse(article.getId());
    }

    @Transactional(readOnly = true)
    public ArticleResponse.HomeArticleResponseList getHomeArticle(List<String> tab, Integer page, String keyword) {

        keyword = (keyword == null) ? "" : keyword.trim();
        Pageable pageable = PageRequest.of(page, 16);

        Page<Article> result;

        if (tab == null || tab.isEmpty()) {
            result = articleRepository.searchByTitle(keyword, pageable);

            return articleMapper.toHomeArticleResponseList(result, result.getTotalElements(),
                    (long) result.getTotalPages());
        }

        List<Long> targetRoleIds = tab.stream().map(target -> notFoundGuardService.getRole(target).getId()).toList();

        result = articleRepository.searchByTitleAndAllRoles(keyword, targetRoleIds, targetRoleIds.size(), pageable);

        return articleMapper.toHomeArticleResponseList(result, result.getTotalElements(),
                (long) result.getTotalPages());
    }

    @Transactional
    public Long readArticle(Long articleId) {
        Article article = articleRepository.getArticle(articleId);

        article.increaseClickCount();

        return article.getClickCount();
    }
}
