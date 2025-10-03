package com.example.skillup.domain.event.entity;

import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "event_view_daily",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "view_date"})
)
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventViewDaily extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "event_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_event_view_daily_event"))
    private Event event;

    @Column(name = "view_date", nullable = false)
    private LocalDate viewDate;

    @Column(name = "cnt", nullable = false)
    @Builder.Default
    private long cnt = 0L;

}
