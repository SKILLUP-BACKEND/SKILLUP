package com.example.skillup.domain.event.entity;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = PROTECTED)
@AllArgsConstructor
@Builder
@SQLRestriction("deleted_at IS NULL")
public class Event extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column
    private String thumbnailUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private EventCategory category;

    // 행사기간

    @Column(nullable = false)
    private LocalDateTime eventStart;
    private LocalDateTime eventEnd;

    // 모집기간

    @Column(nullable = false)
    private LocalDateTime recruitStart;
    private LocalDateTime recruitEnd;

    // 참가비

    @Column(nullable = false)
    private Boolean isFree;
    private Integer price;

    @Builder.Default
    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "event_target_role",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<TargetRole> targetRoles = new HashSet<>();
    // 장소
    private Boolean isOnline;
    private String locationText;
    private String locationLink;

    // 신청 링크
    private String applyLink;

    // 임시저장 or 등록
    @Enumerated(EnumType.STRING)
    private EventStatus status;

    // 문의 방법
    private String contact;

    // 행사 설명
    @Column(columnDefinition = "TEXT")
    private String description;
    //최대 다섯개
    @Column(columnDefinition = "TEXT")
    private String hashtags;

    @Column(name = "views_count", nullable = false)
    private long viewsCount = 0L;

    @Column(name = "likes_count", nullable = false)
    private long likesCount = 0L;

    @Column(name = "apply_clicks", nullable = false)
    private long applyClicks = 0L;         // 신청 버튼 클릭 수

    @Column(name = "apply_impressions" , nullable = false)
    private long applyImpressions = 0L;    // 신청 노출 수
    //TODO : 신청 클릭률 계산 방식 질문하기

    // 운영 태그
    @Column(name = "recommended_manual", nullable = false)
    private boolean recommendedManual = false; // 운영진 수동 추천

    @Column(name = "ad_flag", nullable = false)
    private boolean ad = false;                // 광고/제휴 노출 여부




    public void addTargetRole(TargetRole role) {
        targetRoles.add(role);
        role.getEvents().add(this);
    }


    public void update(EventRequest.UpdateEvent request) {
        this.title = request.getTitle();
        this.thumbnailUrl = request.getThumbnailUrl();
        this.category = request.getCategory();
        this.eventStart = request.getEventStart();
        this.eventEnd = request.getEventEnd();
        this.recruitStart = request.getRecruitStart();
        this.recruitEnd = request.getRecruitEnd();
        this.isFree = request.getIsFree();
        this.price = request.getPrice();
        this.isOnline = request.getIsOnline();
        this.locationText = request.getLocationText();
        this.locationLink = request.getLocationLink();
        this.applyLink = request.getApplyLink();
        this.status = request.isDraft() ? EventStatus.DRAFT : EventStatus.PUBLISHED;
        this.contact = request.getContact();
        this.description = request.getDescription();
        this.hashtags = request.getHashtags();
    }
}
