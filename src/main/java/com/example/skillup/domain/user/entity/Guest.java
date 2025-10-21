package com.example.skillup.domain.user.entity;

import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
public class Guest  extends BaseEntity
{
    @Id
    @Column(length = 100)
    private String guestId; // 쿠키로 발급한 UUID 값 (예: guest_83a2f1d0...)

    //Guest 쿠키 값 유효 시간을 정해야 할 거 같음
}
