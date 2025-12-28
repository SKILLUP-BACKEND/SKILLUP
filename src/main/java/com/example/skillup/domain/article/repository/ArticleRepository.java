package com.example.skillup.domain.article.repository;

import com.example.skillup.domain.article.entity.Article;
import com.example.skillup.domain.article.enums.ArticleStatus;
import com.example.skillup.domain.article.exception.ArticleErrorCode;
import com.example.skillup.domain.article.exception.ArticleException;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    default Article getArticle(Long articleId) {
        return findById(articleId).orElseThrow(
                () -> new ArticleException(ArticleErrorCode.ARTICLE_ENTITY_NOT_FOUND, "Article_ID 가 " + articleId
                        + "인"));
    }

    @Query("""
               select a from Article a
               where a.status = :articleStatus
                 and (:keyword is null or a.title like concat('%', :keyword, '%') or a.source like concat('%', :keyword, '%'))
            """)
    Page<Article> findByStatusAndKeyword(@Param("articleStatus") ArticleStatus articleStatus,
                                         @Param("keyword") String keyword, Pageable pageable);


    @Query(
            value = """
                        SELECT a
                        FROM Article a
                        WHERE a.title LIKE CONCAT('%', :keyword, '%')
                        ORDER BY
                          CASE WHEN a.title = :keyword THEN 0 ELSE 1 END ASC,
                          a.originalPublishedDate DESC,
                          a.clickCount DESC
                    """,
            countQuery = """
                        SELECT COUNT(a)
                        FROM Article a
                        WHERE a.title LIKE CONCAT('%', :keyword, '%')
                    """
    )
    Page<Article> searchByTitle(@Param("keyword") String keyword, Pageable pageable);

    @Query(
            value = """
                        SELECT a
                        FROM Article a
                        WHERE a.title LIKE CONCAT('%', :keyword, '%')
                          AND (
                            SELECT COUNT(DISTINCT r.id)
                            FROM a.targetRoles r
                            WHERE r.id IN :roleIds
                          ) = :roleCount
                        ORDER BY
                          CASE WHEN a.title = :keyword THEN 0 ELSE 1 END ASC,
                          a.originalPublishedDate DESC,
                          a.clickCount DESC
                    """,
            countQuery = """
                        SELECT COUNT(a)
                        FROM Article a
                        WHERE a.title LIKE CONCAT('%', :keyword, '%')
                          AND (
                            SELECT COUNT(DISTINCT r.id)
                            FROM a.targetRoles r
                            WHERE r.id IN :roleIds
                          ) = :roleCount
                    """
    )
    Page<Article> searchByTitleAndAllRoles(
            @Param("keyword") String keyword,
            @Param("roleIds") List<Long> roleIds,
            @Param("roleCount") int roleCount,
            Pageable pageable
    );


    @Query("""
            select distinct a
            from Article a
            join a.targetRoles tr
            where a.status = :status
              and tr.name = :roleName
            order by a.originalPublishedDate desc
            """)
    List<Article> findLatestByRole(ArticleStatus status, String roleName, Pageable pageable);

    List<Article> findTop5ByStatusOrderByOriginalPublishedDateDesc(ArticleStatus articleStatus);
}
