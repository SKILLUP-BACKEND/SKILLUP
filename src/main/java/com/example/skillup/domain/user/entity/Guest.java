package com.example.skillup.domain.user.entity;

import static lombok.AccessLevel.PROTECTED;

import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class Guest  extends BaseEntity
{
    @Id
    @Column(length = 100)
    private String guestId; // 쿠키로 발급한 UUID 값 (예: guest_83a2f1d0...)

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    public void extendExpiration(long days) {
        this.expiredAt = LocalDateTime.now().plusDays(days);
    }
}
