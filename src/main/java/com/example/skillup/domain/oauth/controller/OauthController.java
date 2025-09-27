package com.example.skillup.domain.oauth.controller;

import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.oauth.service.OauthService;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.global.auth.dto.response.TokenResponse;
import com.example.skillup.global.auth.service.AuthService;
import com.example.skillup.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/oauth")
@Slf4j
@Tag(name = "OAuth", description = "소셜 로그인 인증을 시작하고 콜백을 처리하는 API를 제공합니다.")
public class OauthController {

    private final OauthService oauthService;
    private final AuthService authService;
    @Operation(
            summary = "소셜 로그인 프로세스 시작",
            description = "이 API는 사용자를 소셜 로그인 페이지로 리다이렉트하여 인증 절차를 시작합니다.",
            operationId = "startSocialLogin",
            parameters = {
                    @Parameter(name = "socialLoginType", description = "소셜 로그인 유형 (예: Kakao, Google 등).", required = true)
            })
    @GetMapping(value = "/{socialLoginType}")
    public BaseResponse<String> socialLoginType(
            @PathVariable(name = "socialLoginType") SocialLoginType socialLoginType)
    {
        String redirectURL = oauthService.request(socialLoginType);
        return BaseResponse.success("리다이렉트 될 소셜 로그인 페이지 주소입니다.","SOCIAL_LOGIN_TYPE : "+redirectURL);
    }



    @Operation(
            summary = "소셜 로그인 콜백 처리",
            description = "소셜 로그인 후 콜백으로 받은 코드로 액세스 토큰을 요청하고, 소셜 서버의 사용자 정보를 기반으로 자체 토큰을 발급해 로그인 처리합니다.",
            operationId = "handleSocialLoginCallback",
            parameters = {
                    @Parameter(name = "socialLoginType", description = "소셜 로그인 유형", required = true),
                    @Parameter(name = "code", description = "소셜 로그인 API 서버로부터 받은 인증 코드.", required = true)
            })
    @GetMapping(value = "/{socialLoginType}/callback")
    public BaseResponse<TokenResponse> callback(
            @PathVariable(name = "socialLoginType") SocialLoginType socialLoginType,
            @RequestParam(name = "code") String code,
            @RequestParam String state) {


        Long userId = oauthService.requestAccessTokenAndSaveUser(socialLoginType, code);

        TokenResponse tokenResponse = authService.login(userId, "users");

        return BaseResponse.success("관리자 로그인에 성공했습니다.",tokenResponse);

    }
}