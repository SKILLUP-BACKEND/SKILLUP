package com.example.skillup.domain.article.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.skillup.domain.article.dto.request.ArticleRequest;
import com.example.skillup.domain.article.dto.response.ArticleResponse;
import com.example.skillup.domain.article.dto.response.ArticleResponse.AdminArticleDetailsResponse;
import com.example.skillup.domain.article.dto.response.ArticleResponse.AdminArticleResponseList;
import com.example.skillup.domain.article.entity.Article;
import com.example.skillup.domain.article.enums.ArticleStatus;
import com.example.skillup.domain.article.exception.ArticleErrorCode;
import com.example.skillup.domain.article.exception.ArticleException;
import com.example.skillup.domain.article.mapper.ArticleMapper;
import com.example.skillup.domain.article.repository.ArticleRepository;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.global.enums.JobGroup;
import com.example.skillup.global.service.NotFoundGuardService;
import com.example.skillup.global.service.S3Service;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
public class ArticleServiceTest {


    @Mock
    private ArticleRepository articleRepository;
    @Mock
    private ArticleMapper articleMapper;
    @Mock
    private S3Service s3Service;
    @Mock
    private TargetRoleRepository targetRoleRepository;

    @Mock
    private NotFoundGuardService notFoundGuardService;

    @InjectMocks
    private ArticleService articleService;



    /*
    아티클 테스트 코드 작성
    5. 아티클 수정 테스트
    * */

    @Test
    @DisplayName("아티클 생성 성공 테스트")
    public void Success_Create_Article() {
        //given

        List<String> roleNames = List.of("디자이너");

        ArticleRequest.CreateArticleRequest request = new ArticleRequest.CreateArticleRequest("test", "summary",
                "test.com", ArticleStatus.PUBLISHED, "source", LocalDate.now(), roleNames);

        String expectedUrl = "https://s3.bucket/article/thumbnail/image.jpg";
        Article mockarticle = Article.builder().id(1L).title("test").summary("summary").originalUrl("test.com")
                .status(ArticleStatus.PUBLISHED).source("source").thumbnailUrl(expectedUrl).build();

        TargetRole designRole = TargetRole.builder().id(1L).name("디자이너").build();
        TargetRole devRole = TargetRole.builder().id(2L).name("개발자").build();

        MultipartFile multipartFile = Mockito.mock(MultipartFile.class);

        given(s3Service.uploadFile(multipartFile, "article/thumbnail")).willReturn(expectedUrl);

        given(articleMapper.toArticleEntity(request, expectedUrl)).willReturn(mockarticle);
        given(notFoundGuardService.getRole(anyString())).willReturn(designRole);
        given(articleRepository.save(mockarticle)).willReturn(mockarticle);

        //when

        Article result = articleService.createArticle(request, multipartFile);

        //then
        assertThat(result).isNotNull();
        assertThat(result.getTitle()).isEqualTo("test");
        assertThat(result.getOriginalUrl()).isEqualTo("test.com");
        assertThat(result.getStatus()).isEqualTo(ArticleStatus.PUBLISHED);
        assertThat(result.getTargetRoles()).extracting("name").contains(designRole.getName());
        assertThat(result.getSource()).isEqualTo("source");
        assertThat(result.getThumbnailUrl()).isEqualTo(expectedUrl);
        assertThat(result.getTargetRoles()).hasSize(1);

        verify(articleRepository).save(mockarticle);
        verify(s3Service).uploadFile(multipartFile, "article/thumbnail");
        verify(articleMapper).toArticleEntity(request, expectedUrl);

    }

    @Test
    @DisplayName("아티클 삭제 성공 테스트")
    public void delete_Article_Success() {
        //given
        Long articleId = 1L;
        Article mockArticle = Article.builder().id(articleId).build();
        given(articleRepository.getArticle(articleId)).willReturn(mockArticle);

        //when
        System.out.println(mockArticle.getDeletedAt());
        ArticleResponse.CommonArticleResponse articleResponse = articleService.deleteArticle(articleId);
        System.out.println(mockArticle.getDeletedAt());

        //then
        assertThat(articleResponse.getId()).isEqualTo(articleId);
        assertThat(mockArticle.getDeletedAt()).isNotNull();

        verify(articleRepository).getArticle(articleId);
    }

    @Test
    @DisplayName("아티클 조회수 증가 성공 테스트")
    public void read_Article_Success() {
        Long articleId = 1L;
        Article mockArticle = Article.builder().id(articleId).build();
        given(articleRepository.getArticle(articleId)).willReturn(mockArticle);

        System.out.println("신청 횟수 : " + mockArticle.getClickCount());
        articleService.readArticle(articleId);
        System.out.println("신청 횟수 : " + mockArticle.getClickCount());

        assertThat(mockArticle.getClickCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("아티클 삭제 실패 테스트(아티클이 존재하지 않을때)")
    public void delete_Article_Fail() {
        //given
        Long articleId = 999L;

        ArticleErrorCode errorCode = ArticleErrorCode.ARTICLE_ENTITY_NOT_FOUND;
        String errorMessage = errorCode.getMessage();

        given(articleRepository.getArticle(articleId)).willThrow(
                new ArticleException(errorCode, "Article_ID 가 " + articleId + "인"));

        //when&then
        assertThatThrownBy(() -> articleService.deleteArticle(articleId)).isInstanceOf(ArticleException.class)
                .hasMessageContaining(errorMessage);
    }

    @Test
    @DisplayName("아티클 조회수 증가 실패 테스트(아티클이 존재하지 않을때)")
    public void read_Article_Fail() {
        Long articleId = 999L;

        ArticleErrorCode errorCode = ArticleErrorCode.ARTICLE_ENTITY_NOT_FOUND;
        String errorMessage = errorCode.getMessage();

        given(articleRepository.getArticle(articleId)).willThrow(
                new ArticleException(errorCode, "Article_ID 가 " + articleId + "인"));

        //when&then
        assertThatThrownBy(() -> articleService.readArticle(articleId)).isInstanceOf(ArticleException.class)
                .hasMessageContaining(errorMessage);

    }

    @Test
    @DisplayName("아티클 상세 조회 성공 테스트(관리자)")
    public void get_Admin_Article_Detail_Success() {
        //given
        Long articleId = 1L;
        Article mockArticle = Article.builder().id(articleId).title("test").build();
        ArticleResponse.AdminArticleDetailsResponse expectedResponse = AdminArticleDetailsResponse.builder()
                .title("test")
                .build();

        given(articleRepository.getArticle(articleId)).willReturn(mockArticle);
        given(articleMapper.toAdminArticleDetailsResponse(mockArticle)).willReturn(expectedResponse);

        //when
        ArticleResponse.AdminArticleDetailsResponse result = articleService.getAdminArticleDetail(articleId);

        assertThat(result).isEqualTo(expectedResponse);
        verify(articleRepository).getArticle(articleId);
        verify(articleMapper).toAdminArticleDetailsResponse(mockArticle);

    }

    @Test
    @DisplayName("아티클 리스트 조회 성공 테스트(관리자)")
    public void get_Admin_Articles_Success() {
        //given
        ArticleStatus articleStatus = ArticleStatus.PUBLISHED;
        int page = 0;
        String keyword = "";
        String sort = "게시일순";

        Page<Article> publishedPage = new PageImpl<>(
                List.of(Article.builder().id(1L).status(ArticleStatus.PUBLISHED).build()));
        Page<Article> draftPage = new PageImpl<>(List.of(Article.builder().id(2L).status(ArticleStatus.DRAFT).build()));

        ArticleResponse.AdminArticleResponseList expectedResponse = AdminArticleResponseList.builder().build();

        given(articleRepository.findByStatusAndKeyword(eq(ArticleStatus.PUBLISHED), anyString(), any())).willReturn(
                publishedPage);
        given(articleRepository.findByStatusAndKeyword(eq(ArticleStatus.DRAFT), anyString(), any())).willReturn(
                draftPage);
        given(articleMapper.toAdminArticleResponseList(publishedPage, draftPage, articleStatus, sort)).willReturn(
                expectedResponse);

        //when
        ArticleResponse.AdminArticleResponseList result = articleService.getAdminArticle(articleStatus, page, keyword,
                sort);

        //then
        assertThat(result).isEqualTo(expectedResponse);

        Sort expectedSort = Sort.by(Sort.Direction.DESC, "originalPublishedDate");
        Pageable expectedPageable = PageRequest.of(page, 10, expectedSort);

        verify(articleRepository).findByStatusAndKeyword(eq(ArticleStatus.PUBLISHED), anyString(),
                eq(expectedPageable));
        verify(articleRepository).findByStatusAndKeyword(eq(ArticleStatus.DRAFT), anyString(), eq(expectedPageable));


    }

    @Test
    @DisplayName("아티클 리스트 조회 실패 (관리자)")
    public void get_Admin_Articles_Fail() {
        //given
        String invalidSort = "인기순";
        ArticleErrorCode errorCode = ArticleErrorCode.INVALID_ARTICLE_SORT_TYPE;
        String errorMessage = errorCode.getMessage();

        //when&then
        assertThatThrownBy(() -> articleService.getAdminArticle(ArticleStatus.PUBLISHED, 0, "", invalidSort))
                .isInstanceOf(ArticleException.class)
                .hasMessageContaining(errorMessage);
    }

    @Test
    @DisplayName("아티클 목록 조회 성공(일반회원) - 탭이 존재하는 경우")
    public void get_User_Article_WithTabs_Success() {
        //given
        Integer page = 0;
        JobGroup tab = JobGroup.DESIGN;
        String keyword = "";

        TargetRole designRole = TargetRole.builder().id(1L).name("디자이너").build();

        ArticleResponse.HomeArticleResponseList expectedResponse = ArticleResponse.HomeArticleResponseList.builder()
                .build();
        Page<Article> articlePage = new PageImpl<>(
                List.of(Article.builder().id(1L).status(ArticleStatus.PUBLISHED).build()));

        given(articleMapper.toHomeArticleResponseList(eq(articlePage), any(), any())).willReturn(expectedResponse);
        given(articleRepository.searchByTitleAndRoles(eq(keyword), any(), any())).willReturn(articlePage);
        given(notFoundGuardService.getRole(tab.getToKorean())).willReturn(designRole);

        //when
        ArticleResponse.HomeArticleResponseList result = articleService.getHomeArticle(tab, page, keyword);

        //then
        assertThat(result).isEqualTo(expectedResponse);
        verify(articleRepository).searchByTitleAndRoles(anyString(), eq(1L), any(Pageable.class));
    }

    @Test
    @DisplayName("아티클 목록 조회 성공(일반회원) - 탭이 존재하지 않는 경우")
    public void get_User_Article_NoTabs_Success() {
        //given
        Integer page = 0;
        String keyword = "";
        List<String> emptyTab = List.of();

        Page<Article> articlePage = new PageImpl<>(
                List.of(Article.builder().id(1L).status(ArticleStatus.PUBLISHED).build()));
        ArticleResponse.HomeArticleResponseList expectedResponse = ArticleResponse.HomeArticleResponseList.builder()
                .build();

        given(articleRepository.searchByTitle(any(), any())).willReturn(articlePage);
        given(articleMapper.toHomeArticleResponseList(eq(articlePage), any(), any())).willReturn(expectedResponse);

        //when
        ArticleResponse.HomeArticleResponseList result2 = articleService.getHomeArticle(JobGroup.ALL, page, keyword);

        //then
        assertThat(result2).isEqualTo(expectedResponse);
        verify(articleRepository).searchByTitle(any(), any(Pageable.class));
    }

    @Test
    @DisplayName("관리자 아티클 수정 성공 - 썸네일 이미지 변경 및 역할(Tag) 수정")
    void updateAdminArticle_Success() {
        // given
        Long articleId = 1L;
        String newThumbnailUrl = "https://s3.bucket/new-image.jpg";

        MultipartFile mockFile = Mockito.mock(MultipartFile.class);

        ArticleRequest.AdminUpdateArticleRequest request = new ArticleRequest.AdminUpdateArticleRequest("수정된 제목",
                "수정된 요약", "https://new.url", ArticleStatus.PUBLISHED, "새로운 출처", LocalDate.now(), List.of("기획자"));

        Article article = Article.builder()
                .id(articleId)
                .title("옛날 제목")
                .thumbnailUrl("old.jpg")
                .originalPublishedDate(LocalDate.now().minusDays(1))
                .build();

        TargetRole pmRole = TargetRole.builder().id(1L).name("기획자").build();
        given(articleRepository.getArticle(articleId)).willReturn(article);
        given(s3Service.uploadFile(eq(mockFile), anyString())).willReturn(newThumbnailUrl);
        given(notFoundGuardService.getRole(JobGroup.PM.getToKorean())).willReturn(pmRole);

        // when
        articleService.updateAdminArticle(articleId, request, mockFile);

        //then

        assertThat(article.getThumbnailUrl()).isEqualTo(newThumbnailUrl);
        assertThat(article.getTitle()).isEqualTo("수정된 제목");
        assertThat(article.getTargetRoles()).hasSize(1);
        assertThat(article.getTargetRoles()).extracting("name").containsExactly(pmRole.getName());

        verify(s3Service).uploadFile(mockFile, "article/thumbnail");
    }

}
