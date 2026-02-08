package com.example.skillup.global.auth;

import com.example.skillup.domain.user.entity.UsersDetails;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@TestConfiguration
public class TestAuthControllerConfig {

    @RestController
    @RequestMapping("/test")
    static class AuthPrincipalTestController {

        @GetMapping("/me")
        public Map<String, Object> me(@AuthenticationPrincipal(errorOnInvalidType = false) UsersDetails principal) {
            if (principal == null) return Map.of("principal", null);

            System.out.println(principal);
            boolean isAdmin = principal.getAdmin() != null;
            String email = isAdmin ? principal.getAdmin().getEmail() : principal.getUser().getEmail();

            return Map.of(
                    "type", isAdmin ? "ADMIN" : "USER",
                    "email", email,
                    "userNull", principal.getUser() == null,
                    "adminNull", principal.getAdmin() == null
            );
        }
    }
}