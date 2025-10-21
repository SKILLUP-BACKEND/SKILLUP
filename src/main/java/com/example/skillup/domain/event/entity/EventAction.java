package com.example.skillup.domain.event.entity;

import com.example.skillup.domain.event.enums.ActionType;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@Table(
        name = "event_action",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "event_id", "action_type"})
        }
)

public class EventAction extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Users user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Event event;

    @Enumerated(EnumType.STRING)
    private ActionType actionType; // VIEW, SAVE, APPLY

}