package com.example.skillup.global.auth.service;

import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.enums.UserStatus;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.auth.RefreshToken.RefreshToken;
import com.example.skillup.global.auth.RefreshToken.RefreshTokenRepository;
import com.example.skillup.global.auth.dto.response.TokenResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TargetRoleRepository targetRoleRepository;
    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        TargetRole role=targetRoleRepository.save(TargetRole.builder().name("AI_DEVELOPER").build());

        userRepository.save(Users.builder().email("aa@a").name("sd").regDatetime(LocalDateTime.now())
                .role(role).status(UserStatus.ACTIVE).jobGroup("sd").notificationFlag("n").build());
    }

    @Test
    void login_성공테스트() {
        // given
        String email= "VIEWER";
        String role = "USER";

        // when
        TokenResponse response = authService.login(email, role);

        // then
        assertThat(response).isNotNull();
        assertThat(response.accessToken()).isNotBlank();
        List< RefreshToken> all = refreshTokenRepository.findAll();
        assertThat(all).isNotNull();
    }

    @Test
    void login_성공테스트_리프레쉬토큰_업데이트_체크(){
        // given
        String email= "aa@a";
        String role = "USER";

        authService.login(email, role);
        authService.login(email, role);

        List< RefreshToken> all = refreshTokenRepository.findAll();
        assertThat(all.size()).isEqualTo(1);
    }
}
