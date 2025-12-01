package com.example.skillup.domain.oauth.Entity;


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
public class OauthInfo {
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


    private String socialId;

    @Enumerated(EnumType.STRING)
    private SocialLoginType socialLoginType;

}
