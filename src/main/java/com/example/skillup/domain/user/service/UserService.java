package com.example.skillup.domain.user.service;


import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.Event;
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
import com.example.skillup.domain.user.repository.InquiryRepository;
import com.example.skillup.domain.user.repository.InterestRepository;
import com.example.skillup.global.aop.ConvertNotFound;
import com.example.skillup.global.common.CommonResponse;
import com.example.skillup.global.service.S3Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    public UserResponse.MyPageHomeResponse getMyPageHome(Users user) {
        return userMapper.toMyPageHomeResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse.MyPageBookMarkResponse getMyPageBookMark(Users user, EventCategory category, String sort,
                                                                 int page) {
        List<Event> eventBookmarks = new ArrayList<>();
        Pageable pageable = PageRequest.of(page, 9);

        switch (sort) {
            case "latest" ->
                    eventBookmarks = eventBookmarkRepository.findEventsByUserWithLatest(user, category, pageable);
            case "deadline" ->
                    eventBookmarks = eventBookmarkRepository.findEventsByUserWithDeadLine(user, category, pageable);
        }
        List<EventResponse.HomeEventResponse> eventBookmarksDto = eventBookmarks.stream()
                .map(event -> eventMapper.toFeaturedEvent(event, true, event.isRecommendedManual(), event.isAd(), null))
                .toList();

        CommonResponse.PageInfoResponse pageInfoResponse = CommonResponse.PageInfoResponse
                .builder()
                .currentPage(page + 1)
                .pageSize(pageable.getPageSize())
                .totalPages((int) Math.ceil((double) eventBookmarksDto.size() / (pageable.getPageSize())))
                .build();

        List<EventResponse.HomeEventResponse> onGoingEvents = new ArrayList<>();
        List<EventResponse.HomeEventResponse> completedEvents = new ArrayList<>();

        for (EventResponse.HomeEventResponse event : eventBookmarksDto) {
            if (event.getD_dayLabel().equals("마감")) {
                completedEvents.add(event);
            } else {
                onGoingEvents.add(event);
            }
        }
        return userMapper.toMyPageBookMarkResponse(user, onGoingEvents, completedEvents, pageInfoResponse);
    }

    @Transactional(readOnly = true)
    public List<UserResponse.InterestResponse> getInterestByRole(TargetRole role) {
        return interestRepository.findByRole(role).stream().map(userMapper::toInterestResponse).toList();
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
        return userMapper.toUserProfileResponse(user);
    }

    public List<UserResponse.InquiryResponse> getAllInquiry() {
        return inquiryRepository.findAll().stream().map(userMapper::toInquiryResponse).toList();
    }
}
