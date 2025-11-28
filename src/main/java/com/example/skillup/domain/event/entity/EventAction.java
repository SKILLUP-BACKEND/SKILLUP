package com.example.skillup.domain.event.entity;

import static lombok.AccessLevel.PROTECTED;

import com.example.skillup.domain.event.enums.ActionType;
import com.example.skillup.domain.event.enums.ActorType;
import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Table(
        name = "event_action",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"actor_id", "event_id", "action_type"})
        }
)
//event_id와 actor_id로 조회가 자주 일어나므로 인덱스 추가하는 방안 고려

public class EventAction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private ActorType actorType; // USER or GUEST

    // user_id or guest_id user_id는 Long이므로 자료형 변형 필요
    @Column(name = "actor_id", nullable = false)
    private String actorId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Event event;

    @Enumerated(EnumType.STRING)
    private ActionType actionType; // VIEW, SAVE, APPLY
}