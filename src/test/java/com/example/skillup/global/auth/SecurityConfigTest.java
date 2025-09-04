package com.example.skillup.global.auth;

import com.example.skillup.domain.admin.entity.Admin;
import com.example.skillup.domain.admin.entity.AdminRole;
import com.example.skillup.domain.admin.repository.AdminRepository;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.auth.jwt.JwtProvider;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtProvider jwtProvider;

    @MockitoBean
    private AdminRepository adminRepository;

    @MockitoBean
    private UserRepository userRepository;

    @Test
    void 권한_없는_사용자_유저_all_테스트() throws Exception {
        mockMvc.perform(get("/user/all"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 로그인_테스트() throws Exception {

        String email = "testUser@gmail.com";
        String password = "1234";

        Admin admin = new Admin(email, password, AdminRole.OWNER);

        given(adminRepository.findByEmail(email))
                .willReturn(Optional.of(admin));

        String jsonBody = """
        {
          "email": "testUser@gmail.com",
          "password": "1234"
        }
        """;
        mockMvc.perform(post("/admin/login").contentType("application/json").content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    void 접근_가능한_역할로_user_all호출() throws Exception {
        String token = jwtProvider.generateToken(null, "VIEWER", Duration.ofHours(1));

        List<Users> users = new java.util.ArrayList<>();
        users.add(Users.builder().build());

        given(userRepository.findAll())
                .willReturn(users);

        mockMvc.perform(get("/user/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void 접근_불가능한_역할로_user_all호출() throws Exception {
        String token = jwtProvider.generateToken(null, "USER", Duration.ofHours(1));


        mockMvc.perform(get("/user/all")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
