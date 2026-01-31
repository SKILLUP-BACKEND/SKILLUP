package com.example.skillup.domain.event.entity;

import static lombok.AccessLevel.PROTECTED;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.global.common.BaseEntity;
import com.example.skillup.global.common.CommonMapper;
import jakarta.persistence.CascadeType;
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
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLRestriction;

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

    @Column(length = 512)
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

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

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

    @Builder.Default
    @ManyToMany(cascade = CascadeType.MERGE)
    @JoinTable(
            name = "event_hash_tags",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "hash_tags_id")
    )
    private Set<HashTag> hashTags = new HashSet<>();


    @Builder.Default
    @Column(name = "views_count", nullable = false)
    private long viewsCount = 0L;

    @Builder.Default
    @Column(name = "likes_count", nullable = false)
    private long likesCount = 0L;

    @Builder.Default
    @Column(name = "apply_clicks", nullable = false)
    private long applyClicks = 0L;         // 신청 버튼 클릭 수

    // 운영 태그
    @Builder.Default
    @Column(name = "recommended_manual", nullable = false)
    private boolean recommendedManual = false; // 운영진 수동 추천

    @Builder.Default
    @Column(name = "ad_flag", nullable = false)
    private boolean ad = false;                // 광고/제휴 노출 여부


    public void addTargetRole(TargetRole role) {
        targetRoles.add(role);
        role.getEvents().add(this);
    }

    public void addHashTag(HashTag tag) {
        hashTags.add(tag);
    }


    public void update(EventRequest.UpdateEvent request, String thumbnailImage) {
        this.title = request.getTitle();
        this.thumbnailUrl = thumbnailImage;
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
    }

    public void updateCoordinates(Double lat, Double lng) {
        this.latitude = CommonMapper.toBigDecimal(lat);
        this.longitude = CommonMapper.toBigDecimal(lng);
    }

}
