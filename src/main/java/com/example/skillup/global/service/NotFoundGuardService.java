package com.example.skillup.global.service;

import com.example.skillup.domain.event.entity.HashTag;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.exception.HashTagErrorCode;
import com.example.skillup.domain.event.exception.TargetRoleErrorCode;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.event.repository.HashTagRepository;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.exception.UserErrorCode;
import com.example.skillup.domain.user.exception.UserException;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.aop.ConvertNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class NotFoundGuardService
{
    private final TargetRoleRepository targetRoleRepository;
    private final HashTagRepository hashTagRepository;
    private final UserRepository userRepository;
    @ConvertNotFound(
            exception = EventException.class,
            errorCodeEnum = TargetRoleErrorCode.class,
            errorCodeName = "TARGET_ROLE_NOT_FOUND"
    )
    public TargetRole getRole(String name) {
        return targetRoleRepository.findByName(name).orElseThrow();
    }

    @ConvertNotFound(
            exception = EventException.class,
            errorCodeEnum = HashTagErrorCode.class,
            errorCodeName = "HASH_TAG_NOT_FOUND"
    )
    public HashTag getHashTag(String name) {
        return hashTagRepository.findByName(name).orElseThrow();
    }

    @ConvertNotFound(
            exception = UserException.class,
            errorCodeEnum = UserErrorCode.class,
            errorCodeName = "USER_ENTITY_NOT_FOUND"
    )
    public Users getUsersNative(Long userId)
    {
        return userRepository.findByIdNative(userId).orElseThrow();

    }
}
