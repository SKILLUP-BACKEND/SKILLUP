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
import com.example.skillup.domain.user.entity.RecentSearch;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.enums.UserStatus;
import com.example.skillup.domain.user.exception.UserErrorCode;
import com.example.skillup.domain.user.exception.UserException;
import com.example.skillup.domain.user.mappers.UserMapper;
import com.example.skillup.domain.user.repository.InquiryRepository;
import com.example.skillup.domain.user.repository.InterestRepository;
import com.example.skillup.domain.user.repository.RecentSearchRepository;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.domain.user.repository.WithdrawReasonCategoryRepository;
import com.example.skillup.global.aop.ConvertNotFound;
import com.example.skillup.global.auth.dto.response.TokenResponse;
import com.example.skillup.global.auth.service.AuthService;
import com.example.skillup.global.common.CommonResponse;
import com.example.skillup.global.exception.CommonErrorCode;
import com.example.skillup.global.service.NotFoundGuardService;
import com.example.skillup.global.service.S3Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private static final int RETENTION_DAYS = 30;

    final private UserMapper userMapper;
    final private EventBookmarkRepository eventBookmarkRepository;
    final private InterestRepository interestRepository;
    final private EventMapper eventMapper;
    final private TargetRoleRepository targetRoleRepository;
    final private InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final WithdrawReasonCategoryRepository withDrawReasonCategoryRepository;
    private final S3Service s3Service;
    private final NotFoundGuardService notFoundGuardService;
    private final RecentSearchRepository recentSearchRepository;
    private final AuthService authService;

    public UserResponse.MyPageHomeResponse getMyPageHome(Users user) {
        return userMapper.toMyPageHomeResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse.MyPageBookMarkResponse getMyPageBookMark(Users user, String sort, int page, String status) {

        Pageable pageable = createBookmarkPageable(page, status, sort);
        LocalDateTime now = LocalDateTime.now();

        //user를 영속성 컨텍스트로 만들기 위해서
        user = notFoundGuardService.getUsersNative(user.getId());

        Page<Event> eventBookmarks = switch (status) {
            case "recruiting" -> eventBookmarkRepository.findRecruitingEventsByUser(user, now, pageable);
            case "closed" -> eventBookmarkRepository.findClosedEventsByUser(user, now, pageable);
            default -> throw new UserException(CommonErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 status 값입니다.");
        };

        List<EventResponse.HomeEventResponse> events = eventBookmarks.getContent().stream()
                .map(event -> eventMapper.toFeaturedEvent(event, true, event.isRecommendedManual(), event.isAd(), null))
                .toList();

        long recruitingCount;
        long closedCount;

        if ("recruiting".equals(status)) {
            recruitingCount = eventBookmarks.getTotalElements();
            closedCount = eventBookmarkRepository.countClosedEventsByUser(user, now);
        } else {
            closedCount = eventBookmarks.getTotalElements();
            recruitingCount = eventBookmarkRepository.countRecruitingEventsByUser(user, now);
        }

        CommonResponse.PageInfoResponse pageInfoResponse = CommonResponse.PageInfoResponse.builder()
                .currentPage(eventBookmarks.getNumber() + 1)
                .pageSize(eventBookmarks.getSize())
                .totalPages(eventBookmarks.getTotalPages())
                .build();

        return userMapper.toMyPageBookMarkResponse(user, events, pageInfoResponse, recruitingCount, closedCount);
    }

    private Pageable createBookmarkPageable(int page, String status, String sort) {
        if ("latest".equals(sort)) {
            return PageRequest.of(page, 9, Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        if ("deadline".equals(sort)) {
            if ("recruiting".equals(status)) {
                return PageRequest.of(page, 9, Sort.by(Sort.Direction.ASC, "event.eventEnd"));
            }
            if ("closed".equals(status)) {
                return PageRequest.of(page, 9, Sort.by(Sort.Direction.DESC, "event.eventEnd"));
            }
        }

        throw new UserException(CommonErrorCode.INVALID_INPUT_VALUE, "유효하지 않은 sort 값입니다.");
    }

    @Transactional(readOnly = true)
    public List<UserResponse.InterestResponse> getInterestByRole(String roleName) {
        TargetRole role = notFoundGuardService.getRole(roleName);
        return interestRepository.findByRole(role).stream().map(userMapper::toInterestResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UserResponse.WithDrawReasonCategoryResponse> getWithDrawReasonCategory() {
        return withDrawReasonCategoryRepository.findAll().stream().map(userMapper::toWithDrawReasonCategoryResponse)
                .toList();
    }

    @ConvertNotFound(
            exception = EventException.class,
            errorCodeEnum = TargetRoleErrorCode.class,
            errorCodeName = "TARGET_ROLE_NOT_FOUND"
    )
    @Transactional
    public UserResponse.UserProfileResponse updateUser(Users user, UserRequest.UserUpdateRequest request,
                                                       MultipartFile profileImage) {
        user = notFoundGuardService.getUsersNative(user.getId());
        TargetRole role = notFoundGuardService.getRole(request.getRole());
        Set<Interest> interests = notFoundGuardService.getInterestFindByNameIn(request.getInterests());
        String userProfileImageUrl = null;
        if (profileImage != null && !profileImage.isEmpty()) {
            userProfileImageUrl = s3Service.uploadFile(profileImage, "user/profile");
        }
        user.update(request, role, interests, userProfileImageUrl);
        return userMapper.toUserProfileResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse.InquiryResponse> getAllInquiry() {
        return inquiryRepository.findAllByOrderByIdAsc().stream().map(userMapper::toInquiryResponse).toList();
    }

    public UserResponse.UserProfileResponse getUsers(Users user) {
        user = notFoundGuardService.getUsersNative(user.getId());
        return userMapper.toUserProfileResponse(user);
    }

    @Transactional
    public void deleteUser(UserRequest.UserWithdrawRequest request, Users user) {
        user = notFoundGuardService.getUsersNative(user.getId());
        user.withdraw(request.getDetail());
    }

    @Transactional
    public void completeSignup(Users user, UserRequest.UserOAuthSignupRequest request) {
        user = notFoundGuardService.getUsersNative(user.getId());
        TargetRole role = notFoundGuardService.getRole(request.getRole());
        Set<Interest> interests = notFoundGuardService.getInterestFindByNameIn(request.getInterests());
        user.update(null, role, interests, null);
    }


    @Transactional
    public UserResponse.RecentSearchListResponse getRecentSearches(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = now.minusDays(RETENTION_DAYS);

        recentSearchRepository.deleteExpiredByUserId(userId, since);

        List<RecentSearch> recentKeywords =
                recentSearchRepository.findTopByUserIdSinceOrderByUpdatedAtDesc(
                        userId, since, PageRequest.of(0, 6)
                );

        return userMapper.toRecentSearchListResponse(recentKeywords);
    }


    @Transactional
    public void saveRecentSearchKeyword(Long userId, String keyword) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = now.minusDays(RETENTION_DAYS);

        recentSearchRepository.deleteExpiredByUserId(userId, since); // 만료된 데이터 정리

        String trimmedKeyword = keyword.trim();

        RecentSearch recent = recentSearchRepository.findByUserIdAndKeyword(userId, trimmedKeyword)
                .map(existing -> {
                    existing.setUpdatedAt();
                    return existing;
                })
                .orElseGet(() -> RecentSearch.builder()
                        .userId(userId)
                        .keyword(trimmedKeyword)
                        .build());

        recentSearchRepository.save(recent);
    }

    @Transactional
    public void deleteRecentSearchKeyword(Long userId, Long recentId) {
        RecentSearch recentSearch = recentSearchRepository.getRecentSearch(recentId);

        if (!recentSearch.getUserId().equals(userId)) {
            throw new UserException(UserErrorCode.RECENT_SEARCH_FORBIDDEN);
        }

        recentSearchRepository.delete(recentSearch);
    }

    @Transactional
    public void deleteAll(Long userId) {
        recentSearchRepository.deleteAllByUserId(userId);
    }

    @Transactional
    public UserResponse.ContinueLoginResponse continueLogin(UserRequest.ContinueLoginRequest request) {
        Users user = userRepository.findBySocialLoginTypeAndSocialIdWithDeleted
                        (request.getSocialLoginType(), request.getSocialId())
                .orElseThrow(() -> new UserException(UserErrorCode.USER_ENTITY_NOT_FOUND, "해당 사용자가 존재하지 않습니다."));

        if (user.getStatus() == UserStatus.WITHDRAWN) {
            user.rejoin();
        }

        TokenResponse tokenResponse = authService.login(user.getEmail(), "users");

        return UserResponse.ContinueLoginResponse.builder().accessToken(tokenResponse.accessToken()).build();
    }
}
