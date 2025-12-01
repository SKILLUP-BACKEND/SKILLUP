package com.example.skillup.domain.user.service;


import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.mappers.UserMapper;
import com.example.skillup.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService
{
    final private UserRepository usersRepository;
    final private UserMapper userMapper;


    public UserResponse.MyPageHomeResponse getMyPageHome(Users user)
    {
        return userMapper.toMyPageHomeResponse(user);
    }

    public UserResponse.MyPageBookMarkResponse getMyPageBookMark(Users user) {
    }
}
