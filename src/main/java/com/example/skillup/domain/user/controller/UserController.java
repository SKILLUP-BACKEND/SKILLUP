package com.example.skillup.domain.user.controller;


import com.example.skillup.domain.user.dto.response.UserResponseDto;
import com.example.skillup.domain.user.service.UserService;
import com.example.skillup.global.auth.service.AuthService;
import com.example.skillup.global.common.BaseResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
    @GetMapping("/all")
    public BaseResponse<List<UserResponseDto>> findAll()
    {
        return  BaseResponse.success("회원조회에 성공했습니다.",userService.findAll());
    }

    @GetMapping("/test-login")
    public BaseResponse<String> testLogin()
    {
        return BaseResponse.success("테스트 용 엑세스 토큰입니다(모든 권한 허용)",authService.login("test@example.com", "users").accessToken());
    }
}
