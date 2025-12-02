package com.example.skillup.domain.user.mappers;

import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.Inquiry;
import com.example.skillup.domain.user.entity.Interest;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.enums.UserStatus;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public static Users of(String email, String name, String socialId, SocialLoginType socialLoginType, String gender, String age, TargetRole role) {
        return Users.builder()
                .email(email)
                .name(name)
                .regDatetime(LocalDateTime.now())
                .status(UserStatus.ACTIVE)
                .role(role)
                .jobGroup("UNKNOWN")
                .notificationFlag("Y")
                .lastLoginAt(LocalDateTime.now())
                .socialId(socialId)
                .socialLoginType(socialLoginType)
                .gender(gender)
                .age(age)
                .build();
    }

    public UserResponse.MyPageHomeResponse toMyPageHomeResponse(Users user) {
        return UserResponse.MyPageHomeResponse.builder()
                .email(user.getEmail())
                .name(user.getName())
                .build();
    }

    public UserResponse.MyPageBookMarkResponse toMyPageBookMarkResponse(Users user, List<EventResponse.HomeEventResponse> onGoingEvents,
            List<EventResponse.HomeEventResponse> completedEvents)
    {
        return UserResponse.MyPageBookMarkResponse.builder()
                .bookmarkCount(onGoingEvents.size()+completedEvents.size())
                .onGoingEvents(onGoingEvents)
                .completedEvents(completedEvents)
                .email(user.getEmail())
                .name(user.getName())
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
                .build();
    }

    public UserResponse.InquiryResponse toInquiryResponse(Inquiry inquiry) {
        return UserResponse.InquiryResponse.builder()
                .question(inquiry.getQuestion())
                .answerContent(inquiry.getAnswerContent())
                .answerTitle(inquiry.getAnswerTitle())
                .build();
    }

}
