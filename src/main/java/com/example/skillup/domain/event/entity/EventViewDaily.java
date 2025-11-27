package com.example.skillup.domain.event.entity;

import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
        name = "event_view_daily",
        uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "view_date"})
)
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
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
