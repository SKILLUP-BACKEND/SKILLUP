package com.example.skillup.domain.user.controller;


import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.UsersDetails;
import com.example.skillup.domain.user.service.UserService;
import com.example.skillup.global.auth.service.AuthService;
import com.example.skillup.global.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;
    private final AuthService authService;


    @GetMapping("/test-login")
    public BaseResponse<String> testLogin()
    {
        return BaseResponse.success("테스트 용 엑세스 토큰입니다(모든 권한 허용)",authService.login("test@example.com", "users").accessToken());
    }


    @GetMapping("/my-page/home")
    public UserResponse.MyPageHomeResponse getMyPageHome(@AuthenticationPrincipal UsersDetails user) {
        return userService.getMyPageHome(user.getUser());
    }

    @GetMapping("/my-page/bookmark")
    public UserResponse.MyPageBookMarkResponse getMyPageBookMark(@AuthenticationPrincipal UsersDetails user) {
        return userService.getMyPageBookMark(user.getUser());
    }
}
