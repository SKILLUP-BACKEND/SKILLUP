package com.example.skillup.domain.user.controller;


import com.example.skillup.domain.user.dto.response.UserResponseDto;
import com.example.skillup.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<List<UserResponseDto>> findAll()
    {
        return  ResponseEntity.status(HttpStatus.OK)
                .body(userService.findAll());
    }
}
