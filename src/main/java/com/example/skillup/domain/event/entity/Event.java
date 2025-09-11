package com.example.skillup.domain.event.entity;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.exception.TargetRoleErrorCode;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
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

    @Column(nullable = false)
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


    public void addTargetRole(TargetRole role) {
        targetRoles.add(role);
        role.getEvents().add(this);
    }

    public void removeTargetRole(TargetRole role) {
        targetRoles.remove(role);
        role.getEvents().remove(this);
    }

    public void updateTargetRoles(List<String> roleNames, TargetRoleRepository targetRoleRepository) {
        this.targetRoles.forEach(role -> role.getEvents().remove(this));
        this.targetRoles.clear();

        roleNames.stream().distinct().forEach(name -> {
            TargetRole role = targetRoleRepository.findByName(name)
                    .orElseThrow(() -> new EventException(TargetRoleErrorCode.TARGET_ROLE_NOT_FOUND, name + "에"));
            this.addTargetRole(role);
        });
    }

    public void update(EventRequest.UpdateEvent request , TargetRoleRepository targetRoleRepository) {
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

        if (request.getTargetRoles() != null && !request.getTargetRoles().isEmpty()) {
            updateTargetRoles(request.getTargetRoles(), targetRoleRepository);
        }
    }
}
