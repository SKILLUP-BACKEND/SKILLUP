package com.example.skillup.domain.article.entity;


import com.example.skillup.domain.article.dto.request.ArticleRequest.AdminUpdateArticleRequest;
import com.example.skillup.domain.article.enums.ArticleStatus;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@SQLRestriction("deleted_at is NULL")
public class Article extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 256, nullable = false)
    private String summary;

    @Column(length = 512, nullable = false)
    private String thumbnailUrl;

    @Column(length = 512, nullable = false)
    private String originalUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ArticleStatus status = ArticleStatus.DRAFT;

    @Column(nullable = false)
    @Builder.Default
    private long clickCount = 0L;

    @Column(nullable = false, length = 100)
    private String source;

    @Column(nullable = false)
    private LocalDate originalPublishedDate;

    @Builder.Default
    @ManyToMany
    @JoinTable(
            name = "article_target_role",
            joinColumns = @JoinColumn(name = "article_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<TargetRole> targetRoles = new HashSet<>();

    public void addTargetRole(TargetRole role) {
        targetRoles.add(role);
    }

    public void updateInfo(AdminUpdateArticleRequest request) {
        this.title = request.getTitle();
        this.summary = request.getSummary();
        this.source = request.getSource();
        this.originalUrl = request.getOriginalUrl();
        this.status = request.getStatus();
        this.originalPublishedDate = request.getOriginalPublishedDate();

    }

    public void increaseClickCount() {
        this.clickCount++;
    }
}
