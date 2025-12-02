package com.example.skillup.domain.user.service;


import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.exception.TargetRoleErrorCode;
import com.example.skillup.domain.event.mapper.EventMapper;
import com.example.skillup.domain.event.repository.EventBookmarkRepository;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.domain.user.dto.request.UserRequest;
import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.Interest;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.mappers.UserMapper;
import com.example.skillup.domain.user.repository.InterestRepository;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.aop.ConvertNotFound;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService
{
    final private UserRepository usersRepository;
    final private UserMapper userMapper;
    final private EventBookmarkRepository eventBookmarkRepository;
    final private InterestRepository interestRepository;
    final private EventMapper eventMapper;
    final private TargetRoleRepository targetRoleRepository;

    public UserResponse.MyPageHomeResponse getMyPageHome(Users user)
    {
        return userMapper.toMyPageHomeResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse.MyPageBookMarkResponse getMyPageBookMark(Users user, EventCategory category)
    {
        List<EventResponse.HomeEventResponse> eventBookmarks=eventBookmarkRepository.findEventsByUser(user,category).stream()
                .map(event -> eventMapper.toFeaturedEvent(event, true, event.isRecommendedManual(), event.isAd(), null))
                .toList();;


        List<EventResponse.HomeEventResponse> onGoingEvents=new ArrayList<>();
        List<EventResponse.HomeEventResponse> completedEvents=new ArrayList<>();

        for(EventResponse.HomeEventResponse event:eventBookmarks)
        {
            if (event.getD_dayLabel().equals("마감"))
                completedEvents.add(event);
            else
                onGoingEvents.add(event);
        }
        return userMapper.toMyPageBookMarkResponse(user,onGoingEvents,completedEvents);
    }

    @Transactional(readOnly = true)
    public List<UserResponse.InterestResponse> getInterestByRole(TargetRole role)
    {
        return interestRepository.findByRole(role).stream().map(userMapper::toInterestResponse).toList();
    }

    @ConvertNotFound(
            exception = EventException.class,
            errorCodeEnum = TargetRoleErrorCode.class,
            errorCodeName = "TARGET_ROLE_NOT_FOUND"
    )
    @Transactional(readOnly = true)
    public UserResponse.UserProfileResponse updateUser(Users user, UserRequest.UserUpdateRequest request)
    {
        TargetRole role=targetRoleRepository.findByName(request.getRole()).orElseThrow();
        Set<Interest> interests=interestRepository.findByNameIn(request.getInterests());
        user.update(request,role,interests);
        return userMapper.toUserProfileResponse(user);
    }
}
