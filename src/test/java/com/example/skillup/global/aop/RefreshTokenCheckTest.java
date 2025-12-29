package com.example.skillup.global.aop;

import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.enums.UserStatus;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.auth.RefreshToken.RefreshToken;
import com.example.skillup.global.auth.RefreshToken.RefreshTokenRepository;
import com.example.skillup.global.auth.jwt.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.startsWith;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class RefreshTokenCheckTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @Autowired
    private TargetRoleRepository targetRoleRepository;
    @BeforeEach
    void setUp() {
        refreshTokenRepository.deleteAll();
        userRepository.deleteAll();
        TargetRole role=targetRoleRepository.save(TargetRole.builder().name("AI_DEVELOPER").build());

        userRepository.save(
                Users.builder()
                        .email("aa@a")
                        .name("sd")
                        .regDatetime(LocalDateTime.now())
                        .role(role)
                        .status(UserStatus.ACTIVE)
                        .notificationFlag("n")
                        .build()
        );
    }



    @Test
    void 유효하지_않은_액세스토큰() throws Exception {
        String expiredToken="sddsddsds";
        mockMvc.perform(get("/events/home/recent")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isUnauthorized());
    }
    @Test
    void 유효_엑세스토큰_재발급X() throws Exception {

        // given
        String email = "aa@a";
        String validAccessToken = jwtProvider.generateToken(email, "users", Duration.ofMinutes(10));

        // when
        mockMvc.perform(get("/events/home/recent")
                        .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk())
                .andExpect(header().doesNotExist("Authorization"));
    }

    @Test
    void 액세스토큰_만료_리프레쉬토큰_유효_재발급O() throws Exception {

        // given
        String email = "aa@a";

        String expiredToken = jwtProvider.generateToken(email, "users", Duration.ofSeconds(-5));

        String refreshToken = jwtProvider.generateToken(email, "users",Duration.ofDays(14));
        refreshTokenRepository.save(RefreshToken.builder().refreshToken(refreshToken).email(email).build());

        mockMvc.perform(get("/events/home/recent")
                        .header("Authorization", "Bearer " + expiredToken))
                .andExpect(status().isOk())
                .andExpect(header().string("Authorization", startsWith("Bearer ")))
                .andDo(print());
    }

    @Test
    void 액세스토큰_만료_리프레쉬토큰_만료_예외O() throws Exception {

        String email = "aa@a";

        String expiredAccessToken = jwtProvider.generateToken(email, "users", Duration.ofSeconds(-5));
        String expiredRefreshToken=jwtProvider.generateToken(email, "users", Duration.ofSeconds(-5));
        refreshTokenRepository.save(RefreshToken.builder().refreshToken(expiredRefreshToken).email(email).build());

        mockMvc.perform(
                        get("/events/home/recent")
                                .header("Authorization", "Bearer " + expiredAccessToken)
                )
                .andDo(print())
                .andExpect(status().isUnauthorized());

    }
}
