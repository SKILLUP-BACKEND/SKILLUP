package com.example.skillup.domain.user.entity;

import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "recent_search",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_recent_search_user_keyword", columnNames = {"user_id", "keyword"})
        },
        indexes = {
                @Index(name = "idx_recent_search_user_updated_at", columnList = "user_id, updated_at DESC"),
                @Index(name = "idx_recent_search_user_keyword", columnList = "user_id, keyword")
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class RecentSearch extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "keyword", nullable = false, length = 200)
    private String keyword;

}
