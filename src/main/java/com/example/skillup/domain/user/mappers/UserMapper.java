package com.example.skillup.domain.user.mappers;

import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.oauth.dto.OauthRequest;
import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.Inquiry;
import com.example.skillup.domain.user.entity.Interest;
import com.example.skillup.domain.user.entity.RecentSearch;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.entity.WithdrawReasonCategory;
import com.example.skillup.domain.user.enums.UserStatus;
import com.example.skillup.global.common.CommonMapper;
import com.example.skillup.global.common.CommonResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public static Users of(String email, String name, String socialId, SocialLoginType socialLoginType, String gender,
                           String age, TargetRole role) {
        return Users.builder()
                .email(email)
                .name(name)
                .regDatetime(LocalDateTime.now())
                .status(UserStatus.ACTIVE)
                .role(role)
                .notificationFlag("Y")
                .lastLoginAt(LocalDateTime.now())
                .socialId(socialId)
                .socialLoginType(socialLoginType)
                .gender(gender)
                .age(age)
                .build();
    }

    public Users fromOauthInfo(
            OauthRequest oauthInfo,
            TargetRole defaultRole
    ) {
        return Users.builder()
                .email(oauthInfo.email())
                .name(
                        oauthInfo.name() != null
                                ? oauthInfo.name()
                                : "OAuthUser"
                )
                .age(oauthInfo.age() != null ? oauthInfo.age() : "0")
                .gender(oauthInfo.gender() != null ? oauthInfo.gender() : "0")
                .socialId(oauthInfo.socialId())
                .socialLoginType(oauthInfo.socialLoginType())
                .status(UserStatus.ACTIVE)
                .role(defaultRole)
                .notificationFlag("N")
                .regDatetime(LocalDateTime.now())
                .lastLoginAt(LocalDateTime.now())
                .marketingAgreement(false)
                .build();
    }

    public UserResponse.MyPageHomeResponse toMyPageHomeResponse(Users user) {
        return UserResponse.MyPageHomeResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }

    public UserResponse.MyPageBookMarkResponse toMyPageBookMarkResponse(Users user,
                                                                        List<EventResponse.HomeEventResponse> eventResponseList,
                                                                        CommonResponse.PageInfoResponse pageInfoResponse,
                                                                        long recruitingCount, long closedCount) {
        return UserResponse.MyPageBookMarkResponse.builder()
                .events(eventResponseList)
                .email(user.getEmail())
                .name(user.getName())
                .recruitingCount(recruitingCount)
                .closedCount(closedCount)
                .bookmarkCount(recruitingCount + closedCount)
                .pageInfo(pageInfoResponse)
                .role(user.getRole().getName())
                .build();
    }

    public UserResponse.InterestResponse toInterestResponse(Interest interest) {
        return UserResponse.InterestResponse.builder()
                .name(interest.getName())
                .build();
    }

    public UserResponse.UserProfileResponse toUserProfileResponse(Users user) {
        return UserResponse.UserProfileResponse.builder()
                .name(user.getName())
                .profileImageUrl(user.getProfileImageUrl())
                .age(user.getAge())
                .gender(user.getGender())
                .interests(user.getInterests().stream().map(Interest::getName).collect(Collectors.toList()))
                .role(user.getRole().getName())
                .marketingAgreement(user.isMarketingAgreement())
                .build();
    }

    public UserResponse.InquiryResponse toInquiryResponse(Inquiry inquiry) {
        return UserResponse.InquiryResponse.builder()
                .question(inquiry.getQuestion())
                .answerContent(inquiry.getAnswerContent())
                .answerTitle(inquiry.getAnswerTitle())
                .build();
    }

    public UserResponse.WithDrawReasonCategoryResponse toWithDrawReasonCategoryResponse
            (WithdrawReasonCategory withDrawReasonCategory) {
        return UserResponse.WithDrawReasonCategoryResponse.builder()
                .description(withDrawReasonCategory.getDescription())
                .build();
    }

    public UserResponse.AdminUserResponse toAdminUserResponse(Users user) {
        return UserResponse.AdminUserResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .createdAt(CommonMapper.toDatePattern(user.getCreatedAt()))
                .email(user.getEmail())
                .socialLoginType(user.getSocialLoginType().getToKorean())
                .role(CommonMapper.convertRole(user.getRole().getName()))
                .status(user.getDeletedAt() != null ? "탈퇴" : "활성").build();
    }

    public UserResponse.AdminUserDetailPageResponse toAdminUserDetailPageResponse(Users user) {
        return UserResponse.AdminUserDetailPageResponse.builder()
                .userId(user.getId())
                .name(user.getName())
                .createdAt(CommonMapper.toDatePattern(user.getCreatedAt()))
                .email(user.getEmail())
                .socialLoginType(user.getSocialLoginType().getToKorean() + " 로그인")
                .role(CommonMapper.convertRole(user.getRole().getName()))
                .lastLoginAt(CommonMapper.toDatePattern(user.getLastLoginAt()))
                .status(user.getDeletedAt() != null ? "탈퇴" : "활성").build();
    }

    public UserResponse.AdminUserEventActionCountsResponse toAdminUserEventActionResponse(int viewCount, int saveCount,
                                                                                          int applyCount) {
        return UserResponse.AdminUserEventActionCountsResponse.builder()
                .viewCount(viewCount)
                .saveCount(saveCount)
                .applyCount(applyCount)
                .build();
    }

    public UserResponse.RecentSearchListResponse toRecentSearchListResponse(List<RecentSearch> recentSearches) {
        return UserResponse.RecentSearchListResponse.builder()
                .items(recentSearches.stream()
                        .map(r -> UserResponse.RecentSearchItem.builder()
                                .id(r.getId())
                                .keyword(r.getKeyword())
                                .build())
                        .toList())
                .build();
    }

}
