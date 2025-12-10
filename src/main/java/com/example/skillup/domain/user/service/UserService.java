package com.example.skillup.domain.user.service;


import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.exception.TargetRoleErrorCode;
import com.example.skillup.domain.event.mapper.EventMapper;
import com.example.skillup.domain.event.repository.EventBookmarkRepository;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.domain.user.dto.request.UserRequest;
import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.Interest;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.entity.UsersDetails;
import com.example.skillup.domain.user.mappers.UserMapper;
import com.example.skillup.domain.user.repository.InquiryRepository;
import com.example.skillup.domain.user.repository.InterestRepository;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.aop.ConvertNotFound;
import com.example.skillup.global.common.CommonResponse;
import com.example.skillup.global.service.S3Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    final private UserMapper userMapper;
    final private EventBookmarkRepository eventBookmarkRepository;
    final private InterestRepository interestRepository;
    final private EventMapper eventMapper;
    final private TargetRoleRepository targetRoleRepository;
    final private InquiryRepository inquiryRepository;
    private final S3Service s3Service;
    private final UserRepository userRepository;

    public UserResponse.MyPageHomeResponse getMyPageHome(Users user) {
        return userMapper.toMyPageHomeResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse.MyPageBookMarkResponse getMyPageBookMark(Users user,String sort,int page)
    {
        List<Event> eventBookmarks=new ArrayList<>();
        Pageable pageable = PageRequest.of(page, 9);

        //user를 영속성 컨텍스트로 만들기 위해서
        user=userRepository.findById(user.getId()).orElse(null);

        switch (sort) {
            case "latest" -> eventBookmarks=eventBookmarkRepository.findEventsByUserWithLatest(user, pageable);
            case "deadline" -> eventBookmarks=eventBookmarkRepository.findEventsByUserWithDeadLine(user, pageable);
        }
        List<EventResponse.HomeEventResponse> eventBookmarksDto=eventBookmarks.stream()
                .map(event -> eventMapper.toFeaturedEvent(event, true, event.isRecommendedManual(), event.isAd(), null))
                .toList();

        CommonResponse.PageInfoResponse pageInfoResponse = CommonResponse.PageInfoResponse
                .builder()
                .currentPage(page+1)
                .pageSize(pageable.getPageSize())
                .totalPages((int) Math.ceil((double) eventBookmarksDto.size() / (pageable.getPageSize())))
                .build();

        List<EventResponse.HomeEventResponse> recruitingEvents=new ArrayList<>();
        List<EventResponse.HomeEventResponse> closedEvents=new ArrayList<>();

        for(EventResponse.HomeEventResponse event:eventBookmarksDto)
        {
            if (event.getD_dayLabel().equals("마감"))
                closedEvents.add(event);
            else
                recruitingEvents.add(event);
        }
        return userMapper.toMyPageBookMarkResponse(user,recruitingEvents,closedEvents,pageInfoResponse);
    }

    @Transactional(readOnly = true)
    public List<UserResponse.InterestResponse> getInterestByRole(String roleName)
    {
        Optional<TargetRole> targetRole=targetRoleRepository.findByName(roleName);
        return interestRepository.findByRole(targetRole.orElseThrow()).stream().map(userMapper::toInterestResponse).toList();
    }

    @ConvertNotFound(
            exception = EventException.class,
            errorCodeEnum = TargetRoleErrorCode.class,
            errorCodeName = "TARGET_ROLE_NOT_FOUND"
    )
    @Transactional
    public UserResponse.UserProfileResponse updateUser(Users user, UserRequest.UserUpdateRequest request,
                                                       MultipartFile profileImage) {
        TargetRole role = targetRoleRepository.findByName(request.getRole()).orElseThrow();
        Set<Interest> interests = interestRepository.findByNameIn(request.getInterests());
        String userProfileImageUrl = s3Service.uploadFile(profileImage, "user/profile");
        user.update(request, role, interests, userProfileImageUrl);
        userRepository.save(user);
        return userMapper.toUserProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse.InquiryResponse> getAllInquiry()
    {
        return inquiryRepository.findAllByOrderByIdAsc().stream().map(userMapper::toInquiryResponse).toList();
    }

    public UserResponse.UserProfileResponse getUsers(Users user) {
        user = userRepository.findById(user.getId()).orElse(null);
        return userMapper.toUserProfileResponse(user);
    }
}
