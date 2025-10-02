package com.example.skillup.domain.event.entity;

import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

@Entity
@Table(name = "event_like",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "user_id"}))
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = PROTECTED)
public class EventLike extends BaseEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @ManyToOne(fetch = LAZY) @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = LAZY) @JoinColumn(name = "user_id", nullable = false)
    private Users user;

    public EventLike(Event event, Users users) {
        this.event = event;
        this.user = users;
    }
}
