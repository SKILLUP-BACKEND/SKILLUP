package com.example.skillup.global.auth;

import com.example.skillup.domain.admin.entity.Admin;
import com.example.skillup.domain.admin.enums.AdminRole;
import com.example.skillup.domain.admin.repository.AdminRepository;
import com.example.skillup.global.auth.jwt.JwtProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestAuthControllerConfig.class)
class AdminUserDetailTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JwtProvider jwtProvider;

    @MockitoBean
    AdminRepository adminRepository;

    @Test
    void Admin_UserDetails_Test() throws Exception {
        String email = "skillup02.official@gmail.com";

        Admin admin = Admin.builder().email(email).build();


        given(adminRepository.findByEmail(email)).willReturn(Optional.of(admin));

        String token = jwtProvider.generateToken(email, AdminRole.OWNER.toString(), Duration.ofHours(1));

        mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/test/me")
                        .header("Authorization", "Bearer " + token)
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("ADMIN"))
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.adminNull").value(false))
                .andExpect(jsonPath("$.userNull").value(true));
    }
}