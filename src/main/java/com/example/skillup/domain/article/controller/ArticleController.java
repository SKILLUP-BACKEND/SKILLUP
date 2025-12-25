package com.example.skillup.domain.article.controller;

import com.example.skillup.domain.article.dto.request.ArticleRequest;
import com.example.skillup.domain.article.dto.response.ArticleResponse;
import com.example.skillup.domain.article.entity.Article;
import com.example.skillup.domain.article.enums.ArticleStatus;
import com.example.skillup.domain.article.service.ArticleService;
import com.example.skillup.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/articles")
@RequiredArgsConstructor
@Tag(name = "Article", description = "아티클 관련 API")
public class ArticleController {

    private final ArticleService articleService;


    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, value = "/admin")
    //@PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "아티클 등록 API",
            description = """
                    아티클 생성 API 입니다.
                    - target_role 타입 : 기획자/디자이너/개발자/AI 개발자 중 중복가능
                    - 이미지 파일(thumbnailImage)은 선택적으로 전송 가능합니다.
                    """
    )
    public BaseResponse<ArticleResponse.CommonArticleResponse> adminCreateArticle(
            @RequestPart @Valid ArticleRequest.CreateArticleRequest request,
            @RequestPart(required = false) MultipartFile thumbnailImage
    ) {
        Article article = articleService.createArticle(request, thumbnailImage);
        String message = article.getStatus() == ArticleStatus.DRAFT ? "아티클이 임시저장 되었습니다." : "아티클이 등록되었습니다.";
        return BaseResponse.success(message, new ArticleResponse.CommonArticleResponse(article.getId()));
    }

    @GetMapping(value = "/admin")
    //@PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "관리자 아티클 조회 API", description = "관리자가 Article 목록을 조회합니다. sort 값은 게시일순 , 등록일순 두가지 입니다. 둘 중 하나를 입력해주세요")
    public BaseResponse<ArticleResponse.AdminArticleResponseList> adminGetArticles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "게시일순") String sort,
            @RequestParam(defaultValue = "PUBLISHED") ArticleStatus status
    ) {
        return BaseResponse.success("관리자 아티클 조회에 성공했습니다.", articleService.getAdminArticle(status, page, keyword, sort));
    }

    @GetMapping(value = "/{articleId}/admin")
    //@PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "관리자 아티클 상세 조회 API", description = "관리자가 Article 목록을 조회합니다. sort 값은 게시일순 , 등록일순 두가지 입니다. 둘 중 하나를 입력해주세요")
    public BaseResponse<ArticleResponse.AdminArticleDetailsResponse> adminGetArticlesDetail(
            @PathVariable Long articleId
    ) {
        return BaseResponse.success("아티클 상세 조회에 성공.", articleService.getAdminArticleDetail(articleId));
    }

    @DeleteMapping(value = "/{articleId}/admin")
    //@PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "관리자 아티클 삭제 API", description = "관리자가 Article 을 삭제합니다 삭제하려는 Article 의 ID 를 입력해주세요")
    public BaseResponse<ArticleResponse.CommonArticleResponse> adminDeleteArticle(
            @PathVariable Long articleId
    ) {
        return BaseResponse.success("아티클 삭제 성공", articleService.deleteArticle(articleId));
    }


    @PutMapping(value = "/{articleId}/admin", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //@PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "아티클 수정 API",
            description = """
                    아티클 수정 API 입니다.
                    - target_role 타입 : 기획자/디자이너/개발자/AI 개발자 중 중복가능
                    - 이미지 파일(thumbnailImage)은 선택적으로 전송 가능합니다.
                    """
    )
    public BaseResponse<ArticleResponse.CommonArticleResponse> adminUpdateArticle(
            @PathVariable Long articleId,
            @RequestPart @Valid ArticleRequest.AdminUpdateArticleRequest request,
            @RequestPart(name = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) {
        return BaseResponse.success("배너 수정 성공", articleService.updateAdminArticle(articleId, request, thumbnailImage));
    }

    @GetMapping()
    @Operation(summary = "아티클 목록 조회 및 검색 API", description = "일반 회원의 아티클 목록 조회 및 검색 API 입니다. 직군을 입력해주세요 (기획자/디자이너/개발자/AI 개발자)")
    public BaseResponse<ArticleResponse.HomeArticleResponseList> GetArticlesDetail(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(required = false) List<String> tab
    ) {
        return BaseResponse.success("아티클 목록 조회 성공.", articleService.getHomeArticle(tab, page, keyword));
    }

    @PostMapping("/read/{articleId}")
    @Operation(summary = "아티클 신청수 증가 API", description = "아티클의 썸네일을 누를때 해당 아티클의 클릭횟수를 늘려주는 API 입니다.")
    public BaseResponse<String> readArticle(@PathVariable Long articleId) {
        Long clickCount = articleService.readArticle(articleId);
        return BaseResponse.success("클릭횟수가 증가했습니다.", "클릭 횟수 : " + clickCount);
    }
}
