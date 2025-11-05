package com.example.skillup.global.search.entity;

import com.example.skillup.global.common.BaseEntity;
import com.example.skillup.global.search.enums.SynonymStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class SynonymGroup extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @Column(nullable=false, length=10)
    @Builder.Default
    private String locale = "ko";

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=20)
    @Builder.Default
    private SynonymStatus status = SynonymStatus.ACTIVE;

    private String comment;

}
