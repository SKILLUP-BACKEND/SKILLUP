package com.example.skillup.global.service;

import com.example.skillup.domain.event.entity.HashTag;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.exception.*;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.event.repository.HashTagRepository;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.domain.user.entity.Interest;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.exception.UserErrorCode;
import com.example.skillup.domain.user.exception.UserException;
import com.example.skillup.domain.user.repository.InterestRepository;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.aop.ConvertNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor

public class NotFoundGuardService
{
    private final TargetRoleRepository targetRoleRepository;
    private final HashTagRepository hashTagRepository;
    private final UserRepository userRepository;
    private final InterestRepository interestRepository;
    @ConvertNotFound(
            exception = EventException.class,
            errorCodeEnum = TargetRoleErrorCode.class,
            errorCodeName = "TARGET_ROLE_NOT_FOUND"
    )
    public TargetRole getRole(String name) {
        if (name == null)
            return null;
        return targetRoleRepository.findByName(name).orElseThrow();
    }

    @ConvertNotFound(
            exception = EventException.class,
            errorCodeEnum = HashTagErrorCode.class,
            errorCodeName = "HASH_TAG_NOT_FOUND"
    )
    public HashTag getHashTag(String name) {
        if(name == null)
            return null;

        return hashTagRepository.findByName(name).orElseThrow();
    }

    @ConvertNotFound(
            exception = UserException.class,
            errorCodeEnum = UserErrorCode.class,
            errorCodeName = "USER_ENTITY_NOT_FOUND"
    )
    public Users getUsersNative(Long userId)
    {
        if(userId == null)
            return null;
        return userRepository.findByIdNative(userId).orElseThrow();

    }

    @ConvertNotFound(
            exception = UserException.class,
            errorCodeEnum = InterestErrorCode.class,
            errorCodeName = "INTEREST_NOT_FOUND"
    )
    public Set<Interest> getInterestFindByNameIn(List<String> interests)
    {
        if(interests == null)
            return null;
        return interestRepository.findByNameIn(interests);
    }
}
