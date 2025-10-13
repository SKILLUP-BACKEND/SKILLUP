package com.example.skillup.global.auth.RefreshToken;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String refreshToken;

    @Column(nullable = false)
    private Long userId;

    public static RefreshToken of(Long userId, String refreshToken) {
        return RefreshToken.builder()
                .userId(userId)
                .refreshToken(refreshToken)
                .build();
    }

    public static RefreshToken of(String refreshToken) {
        return RefreshToken.builder()
                .userId(9999L)
                .refreshToken(refreshToken)
                .build();
    }
}
