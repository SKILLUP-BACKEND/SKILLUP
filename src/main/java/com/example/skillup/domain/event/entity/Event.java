package com.example.skillup.domain.event.entity;

import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
}
