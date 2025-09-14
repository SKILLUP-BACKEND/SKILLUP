package com.example.skillup.domain.user.entity;

import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.user.enums.UserStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static lombok.AccessLevel.PROTECTED;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
public class Users
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String email;

    @Column(nullable = false, length = 15)
    private String password;

    @Column(nullable = false, length = 50)
    private String name;

    private int age;

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

    private String socialId;

    private SocialLoginType socialName;

    public static Users of(String email, String password, String name) {
        return Users.builder()
                .email(email)
                .password(password)
                .name(name)
                .regDatetime(LocalDateTime.now())
                .role("USER")
                .status(UserStatus.ACTIVE)
                .jobGroup("UNKNOWN")
                .notificationFlag("Y")
                .build();
    }

    public boolean isPasswordMatch(String rawPassword) {
        return this.password.equals(rawPassword);
    }
}
