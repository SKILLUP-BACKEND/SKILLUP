package com.example.skillup.domain.article.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.skillup.domain.article.entity.Article;
import com.example.skillup.domain.article.enums.ArticleStatus;
import com.example.skillup.domain.article.exception.ArticleException;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ArticleRepositoryTest {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private TargetRoleRepository targetRoleRepository;

    LocalDate today = LocalDate.now();

    @Test
    @DisplayName("getArticle: 존재하는 ID 조회 시 Article 반환")
    void get_Article_Success() {

        // given
        Article article = createArticle("테스트 제목", ArticleStatus.PUBLISHED, today);
        articleRepository.save(article);

        // when
        Article result = articleRepository.getArticle(article.getId());

        // then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(article.getId());
    }

    @Test
    @DisplayName("getArticle: 존재하지 않는 ID 조회 시 ArticleException 발생")
    void getArticle_fail() {
        // given
        Long nonExistentId = 999L;

        // when & then
        assertThatThrownBy(() -> articleRepository.getArticle(nonExistentId))
                .isInstanceOf(ArticleException.class)
                .hasMessageContaining("Article_ID 가 " + nonExistentId);
    }

    @Test
    @DisplayName("게시일순(originalPublishedDate DESC) 정렬 테스트")
    void findByStatusAndKeyword_SortByPublishedDate() {
        //given
        Article oldArticle = createArticle("Spring Boot Guide", ArticleStatus.PUBLISHED, today.minusDays(10));
        Article newArticle = createArticle("Spring Boot Guide", ArticleStatus.PUBLISHED, today);
        Article middleArticle = createArticle("Spring Boot Guide", ArticleStatus.PUBLISHED, today.minusDays(5));

        Sort sort = Sort.by(Sort.Direction.DESC, "originalPublishedDate");

        Pageable pageable = PageRequest.of(0, 10, sort);

        //when
        Page<Article> result = articleRepository.findByStatusAndKeyword(ArticleStatus.PUBLISHED, "", pageable);

        //then
        List<Article> content = result.getContent();
        assertThat(content).hasSize(3);
        assertThat(result.getContent().get(0)).isEqualTo(newArticle);
        assertThat(result.getContent().get(1)).isEqualTo(middleArticle);
        assertThat(result.getContent().get(2)).isEqualTo(oldArticle);
    }

    @Test
    @DisplayName("findByStatusAndKeyword: 등록일순(createdAt DESC) 정렬 테스트")
    void findByStatusAndKeyword_SortByCreatedAt() throws InterruptedException {
        // given
        Article first = Article.builder()
                .title("첫번째")
                .status(ArticleStatus.PUBLISHED)
                .source("source")
                .summary("summary")
                .thumbnailUrl("thumbnailUrl")
                .originalPublishedDate(today)
                .originalUrl("originalUrl")
                .build();
        Article second = Article.builder()
                .title("두번째")
                .status(ArticleStatus.PUBLISHED)
                .source("source")
                .summary("summary")
                .thumbnailUrl("thumbnailUrl")
                .originalPublishedDate(today)
                .originalUrl("originalUrl")
                .build();
        Article third = Article.builder()
                .title("세번째")
                .status(ArticleStatus.PUBLISHED)
                .source("source")
                .summary("summary")
                .thumbnailUrl("thumbnailUrl")
                .originalPublishedDate(today)
                .originalUrl("originalUrl")
                .build();

        ReflectionTestUtils.setField(first, "createdAt", LocalDateTime.now().minusHours(2)); // 2시간 전
        ReflectionTestUtils.setField(second, "createdAt", LocalDateTime.now().minusHours(1)); // 1시간 전
        ReflectionTestUtils.setField(third, "createdAt", LocalDateTime.now()); // 지금

        articleRepository.save(first);
        articleRepository.save(second);
        articleRepository.save(third);

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(0, 10, sort);

        // when
        Page<Article> result = articleRepository.findByStatusAndKeyword(
                ArticleStatus.PUBLISHED,
                "",
                pageable
        );

        // then
        List<Article> content = result.getContent();
        assertThat(content).hasSize(3);

        assertThat(content.get(0)).isEqualTo(third);
        assertThat(content.get(1)).isEqualTo(second);
        assertThat(content.get(2)).isEqualTo(first);
    }

    @Test
    @DisplayName("findByStatusAndKeyword: 키워드 검색 + 정렬 동시 적용")
    void findByStatusAndKeyword_KeywordAndSort() {
        // given
        createArticle("Spring Data JPA", ArticleStatus.PUBLISHED, today.minusDays(1));
        createArticle("Spring Security", ArticleStatus.PUBLISHED, today);
        createArticle("Python Basic", ArticleStatus.PUBLISHED, today.plusDays(1));

        Sort sort = Sort.by(Sort.Direction.DESC, "originalPublishedDate");
        Pageable pageable = PageRequest.of(0, 10, sort);

        // when
        Page<Article> result = articleRepository.findByStatusAndKeyword(
                ArticleStatus.PUBLISHED,
                "Spring",
                pageable
        );

        // then
        List<Article> content = result.getContent();
        assertThat(content).hasSize(2);
        // 정렬 검증: Spring Security(오늘) -> Spring Data JPA(어제)
        assertThat(content.get(0).getTitle()).isEqualTo("Spring Security");
        assertThat(content.get(1).getTitle()).isEqualTo("Spring Data JPA");
    }

    @Test
    @DisplayName("searchByTitle: [완전 일치 -> 최신순 -> 클릭순] 정렬 로직 검증")
    void searchByTitle_SortLogic() {
        // given
        String keyword = "Java";

        Article partialOld = createArticle("Java Programming", ArticleStatus.PUBLISHED, today.minusDays(10));
        Article partialNew = createArticle("Effective Java", ArticleStatus.PUBLISHED, today);
        Article exactMatch = createArticle("Java", ArticleStatus.PUBLISHED, today.minusDays(100));
        Article sameDateHighClick = createArticle("Java Basic", ArticleStatus.PUBLISHED, today.minusDays(10));
        createArticle("포함되지 않는 아티클", ArticleStatus.PUBLISHED, today);

        partialOld.setClickCount(10L);
        partialNew.setClickCount(20L);
        sameDateHighClick.setClickCount(100L);
        articleRepository.save(partialOld);
        articleRepository.save(partialNew);
        articleRepository.save(sameDateHighClick);

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Article> result = articleRepository.searchByTitle(keyword, pageable);

        // then
        List<Article> content = result.getContent();
        assertThat(content).hasSize(4);

        // 정렬 순서 검증
        // 완전 일치
        assertThat(content.get(0).getTitle()).isEqualTo("Java");

        //부분 일치 중 최신 날짜
        assertThat(content.get(1).getTitle()).isEqualTo("Effective Java");

        //부분일치 + 날짜 동일 + 클릭수 sameDateHighClick 가 더 높음
        assertThat(content.get(2).getTitle()).isEqualTo("Java Basic");
        assertThat(content.get(3).getTitle()).isEqualTo("Java Programming");
    }

    @Test
    @DisplayName("searchByTitleAndAllRoles: 모든 Role을 포함하는 게시글만 조회 (교집합)")
    void searchByTitleAndAllRoles_Filtering() {
        // given
        TargetRole backend = targetRoleRepository.save(createRole("Backend"));
        TargetRole frontend = targetRoleRepository.save(createRole("Frontend"));
        TargetRole devOps = targetRoleRepository.save(createRole("DevOps"));

        Article targetArticle = createArticle("Full Stack Developer", ArticleStatus.PUBLISHED, today);
        targetArticle.addTargetRole(backend);
        targetArticle.addTargetRole(frontend);
        articleRepository.save(targetArticle);

        Article partialArticle = createArticle("Backend Developer", ArticleStatus.PUBLISHED, today);
        partialArticle.addTargetRole(backend);
        articleRepository.save(partialArticle);

        Article otherArticle = createArticle("DevOps Engineer", ArticleStatus.PUBLISHED, today);
        otherArticle.addTargetRole(devOps);
        articleRepository.save(otherArticle);

        Article superArticle = createArticle("CTO", ArticleStatus.PUBLISHED, today);
        superArticle.addTargetRole(backend);
        superArticle.addTargetRole(frontend);
        superArticle.addTargetRole(devOps);
        articleRepository.save(superArticle);

        List<Long> searchRoleIds = List.of(backend.getId(), frontend.getId());
        int roleCount = searchRoleIds.size();
        String keyword = "";

        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<Article> result = articleRepository.searchByTitleAndAllRoles(
                keyword,
                searchRoleIds,
                roleCount,
                pageable
        );

        // then
        List<Article> content = result.getContent();

        assertThat(content).hasSize(2);
        assertThat(content).extracting("title")
                .containsExactlyInAnyOrder("Full Stack Developer", "CTO");
    }

    @Test
    @DisplayName("searchByTitleAndAllRoles: Role 조건은 맞지만 제목이 안 맞으면 제외")
    void searchByTitleAndAllRoles_TitleFiltering() {
        // given
        TargetRole java = targetRoleRepository.save(createRole("Java"));

        Article javaArticle = createArticle("Pure Java", ArticleStatus.PUBLISHED, today);
        javaArticle.addTargetRole(java);
        articleRepository.save(javaArticle);

        Article springArticle = createArticle("Spring Boot", ArticleStatus.PUBLISHED, today);
        springArticle.addTargetRole(java);
        articleRepository.save(springArticle);

        List<Long> roleIds = List.of(java.getId());

        // when
        Page<Article> result = articleRepository.searchByTitleAndAllRoles(
                "Spring", // 키워드 조건
                roleIds,
                roleIds.size(),
                PageRequest.of(0, 10)
        );

        // then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Spring Boot");
    }


    private Article createArticle(String title, ArticleStatus articleStatus, LocalDate publishedDate) {

        Article article = Article.builder()
                .title(title)
                .status(articleStatus)
                .source("source")
                .summary("summary")
                .thumbnailUrl("thumbnailUrl")
                .originalPublishedDate(publishedDate)
                .originalUrl("originalUrl")
                .build();

        return articleRepository.save(article);
    }

    private TargetRole createRole(String name) {
        return TargetRole.builder()
                .name(name)
                .build();
    }
}