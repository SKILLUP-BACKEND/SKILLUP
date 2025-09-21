package com.example.skillup.domain.user.entity;

import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.user.enums.UserStatus;
import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class Users extends BaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String email;

    @Column(nullable = false, length = 50)
    private String name;

    private String age;

    @Column(length = 1)
    private String gender;

    @Column(nullable = false)
    private LocalDateTime regDatetime;

    @Column(length = 10, nullable = false)
    private String role;

    @Column(length = 10, nullable = false)
    private UserStatus status;

    @Column(length = 20, nullable = false)
    private String jobGroup;

    @Column(length = 1, nullable = false)
    private String notificationFlag;

    private LocalDateTime lastLoginAt;

    private Long socialId;

    private SocialLoginType socialLoginType;


}
