package com.example.skillup.domain.user.controller;


import com.example.skillup.domain.user.dto.response.UserResponseDto;
import com.example.skillup.domain.user.service.UserService;
import com.example.skillup.global.common.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;

    @GetMapping("/all")
    public BaseResponse<List<UserResponseDto>> findAll()
    {
        return  BaseResponse.success("회원조회에 성공했습니다.",userService.findAll());
    }
}
