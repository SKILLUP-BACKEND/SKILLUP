package com.example.skillup.domain.admin.controller;

import com.example.skillup.domain.admin.dto.AdminLoginRequest;
import com.example.skillup.domain.admin.entity.Admin;
import com.example.skillup.domain.admin.service.AdminService;
import com.example.skillup.global.auth.dto.response.TokenResponse;
import com.example.skillup.global.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController
{
    private final AuthService authService;
    private final AdminService adminService;
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(
            @RequestBody @Valid AdminLoginRequest request
    )
    {
        Admin admin = adminService.login(request);

        TokenResponse tokenResponse = authService.login(null, admin.getRole().toString());

        return ResponseEntity.ok(tokenResponse);
    }
}
