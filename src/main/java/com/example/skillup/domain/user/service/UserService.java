package com.example.skillup.domain.user.service;


import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventBookmark;
import com.example.skillup.domain.event.mapper.EventMapper;
import com.example.skillup.domain.event.repository.EventBookmarkRepository;
import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.mappers.UserMapper;
import com.example.skillup.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService
{
    final private UserRepository usersRepository;
    final private UserMapper userMapper;
    final private EventBookmarkRepository eventBookmarkRepository;
    final private EventMapper eventMapper;

    public UserResponse.MyPageHomeResponse getMyPageHome(Users user)
    {
        return userMapper.toMyPageHomeResponse(user);
    }

    @Transactional
    public UserResponse.MyPageBookMarkResponse getMyPageBookMark(Users user)
    {
        List<EventResponse.HomeEventResponse> eventBookmarks=eventBookmarkRepository.findEventsByUser(user).stream()
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
}
