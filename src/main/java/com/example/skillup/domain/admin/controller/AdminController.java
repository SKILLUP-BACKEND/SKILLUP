package com.example.skillup.domain.admin.controller;

import com.example.skillup.domain.admin.dto.AdminLoginRequest;
import com.example.skillup.domain.admin.entity.Admin;
import com.example.skillup.domain.admin.service.AdminService;
import com.example.skillup.domain.event.search.service.EventIndexerService;
import com.example.skillup.global.auth.dto.response.TokenResponse;
import com.example.skillup.global.auth.service.AuthService;
import com.example.skillup.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
    private final EventIndexerService eventIndexerService;

    @PostMapping("/login")
    public BaseResponse<TokenResponse> login(
            @RequestBody @Valid AdminLoginRequest request
    )
    {
        Admin admin = adminService.login(request);

        TokenResponse tokenResponse = authService.login(null, admin.getRole().toString());

        return BaseResponse.success("관리자 로그인에 성공했습니다.",tokenResponse);
    }

    @PostMapping("search")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "초기 존재하는 행사 인덱싱 api", description = "ElasticSearch 초기 세팅시 존재하는 행사들을 추가하는 api")
    public BaseResponse<String>  reindexAll(){
        long total = eventIndexerService.bulkIndexAll();
        return BaseResponse.success("현재 존재하는 행사 값들을 인덱싱 작업을 성공했습니다." , "변환된 행사의 개수 : " + total);
    }
}
