package com.example.skillup.domain.user.service;


import com.example.skillup.domain.user.dto.response.UserResponseDto;
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

    public List<UserResponseDto> findAll()
    {
        return toDtoList(usersRepository.findAll());
    }

    //@ThrowIfEmpty
   //public List<UserResponseDto> findDeletedUsers() {}

    private List<UserResponseDto> toDtoList(List<Users> users)
    {
            return users.stream()
                    .map(UserMapper::from)
                    .toList();
    }


}
