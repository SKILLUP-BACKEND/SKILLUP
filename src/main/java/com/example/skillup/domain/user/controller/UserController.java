package com.example.skillup.domain.user.controller;


import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.user.dto.request.UserRequest;
import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.UsersDetails;
import com.example.skillup.domain.user.service.UserService;
import com.example.skillup.global.auth.service.AuthService;
import com.example.skillup.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController
{
    private final UserService userService;
    private final AuthService authService;


    @GetMapping("/test-login")
    public BaseResponse<String> testLogin()
    {
        return BaseResponse.success("테스트 용 엑세스 토큰입니다(모든 권한 허용)",authService.login("test@example.com", "users").accessToken());
    }


    @GetMapping("/my-page/home")
    @Operation(summary = "마이페이지 첫 홈 화면 API로 유저의 이메일과 이름을 리턴합니다."
            , description = "마이페이지 첫 홈 화면 API")
    public BaseResponse<UserResponse.MyPageHomeResponse> getMyPageHome(@AuthenticationPrincipal UsersDetails user) {
        return BaseResponse.success("마이 페이지 홈 조회 성공",userService.getMyPageHome(user.getUser()));
    }

    @GetMapping("/my-page/bookmark")
    @Operation(summary = "마이페이지에서 유저가 북마크 한 이벤트 불러옵니다.(sort 값 latest, deadline)"
            , description = "마이페이지 북마크 페이지 API")
    public  BaseResponse<UserResponse.MyPageBookMarkResponse> getMyPageBookMark(
            @AuthenticationPrincipal UsersDetails user,
            @RequestParam EventCategory category,
            @Parameter(
                    description = "정렬 기준 (latest, deadline)"
            )
            @RequestParam String sort,
            @RequestParam int page
    ) {
        return  BaseResponse.success("마이페이지 북마크된 이벤트 조회 성공",userService.getMyPageBookMark(user.getUser(),category,sort,page));
    }

    @GetMapping("/my-page/profile/interest")
    @Operation(summary = "유저 프로필상 직무별 관심사를 불러옵니다(관심사의 유지 보수 및 정합성 관리를 위하여 DB 레벨에서 관리) "
            , description = "유저 프로필상 직무별 관심사를 가져오는 API ")
    public BaseResponse<List<UserResponse.InterestResponse>> getInterestByRole(@RequestParam String roleName)
    {
        return BaseResponse.success("직무별 관심사 조회 성공",userService.getInterestByRole(roleName));
    }

    @PutMapping("/my-page/profile/update")
    @Operation(summary = "유저 프로필을 업데이트 합니다."
            , description = "유저 프로필 업데이트 API")
    public BaseResponse<UserResponse.UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UsersDetails userDetails,
            @RequestBody UserRequest.UserUpdateRequest request
    ) {
        userService.updateUser(userDetails.getUser(), request);
        return BaseResponse.success("유저 업데이트 성공", userService.updateUser(userDetails.getUser(), request));
    }

    @GetMapping("/my-page/qna")
    @Operation(summary = "마이페이지 고객센터 문의 목록 조회", description = "사용자가 마이페이지에서 등록한 모든 문의 내역을 조회하는 API")
    public BaseResponse<List<UserResponse.InquiryResponse>> getAllInquiry()
    {
        return BaseResponse.success("모든 문의 내용 조회 성공", userService.getAllInquiry());
    }

}
