package com.example.skillup.domain.event.entity;


import com.example.skillup.domain.event.enums.BannerType;
import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class EventBanner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    public Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BannerType type;

    @Column(nullable = false)
    public String bannerImageUrl;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    private boolean selected = false;

    @Column
    private String bannerLink;

    //화면 정렬 순서(작을수록 위로 정렬)
    @Column(nullable = false, name = "display_order")
    private int displayOrder;

    //노출 시작/종료
    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column
    private LocalDateTime endAt; //null 이면 무기한
}
