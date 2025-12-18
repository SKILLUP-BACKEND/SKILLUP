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
import java.util.List;
import java.util.prefs.BackingStoreException;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    @GetMapping()
    public BaseResponse<UserResponse.UserProfileResponse> getUsers(@AuthenticationPrincipal UsersDetails user)
    {
        return BaseResponse.success("유저 조회 성공"
        ,userService.getUsers(user.getUser()));

    }

    @GetMapping("/test-login")
    public BaseResponse<String> testLogin() {
        return BaseResponse.success("테스트 용 엑세스 토큰입니다(모든 권한 허용)",
                authService.login("test@example.com", "users").accessToken());
    }


    @GetMapping("/my-page/home")
    @Operation(summary = "마이페이지 첫 홈 화면 API로 유저의 이메일과 이름을 리턴합니다."
            , description = "마이페이지 첫 홈 화면 API")
    public BaseResponse<UserResponse.MyPageHomeResponse> getMyPageHome(@AuthenticationPrincipal UsersDetails user) {
        return BaseResponse.success("마이 페이지 홈 조회 성공", userService.getMyPageHome(user.getUser()));
    }

    @GetMapping("/my-page/bookmark")
    @Operation(summary = "마이페이지에서 유저가 북마크 한 이벤트 불러옵니다.(sort 값 latest, deadline)"
            , description = "마이페이지 북마크 페이지 API")
    public BaseResponse<UserResponse.MyPageBookMarkResponse> getMyPageBookMark(
            @AuthenticationPrincipal UsersDetails user,
            @Parameter(
                    description = "정렬 기준 (latest, deadline)"
            )
            @RequestParam String sort,
            @RequestParam int page
    ) {
        return BaseResponse.success("마이페이지 북마크된 이벤트 조회 성공",
                userService.getMyPageBookMark(user.getUser(), sort, page));
    }

    @GetMapping("/my-page/profile/interest")
    @Operation(summary = "유저 프로필상 직무별 관심사를 불러옵니다(관심사의 유지 보수 및 정합성 관리를 위하여 DB 레벨에서 관리) "
            , description = "유저 프로필상 직무별 관심사를 가져오는 API ")
    public BaseResponse<List<UserResponse.InterestResponse>> getInterestByRole(@RequestParam String roleName)
    {
        return BaseResponse.success("직무별 관심사 조회 성공",userService.getInterestByRole(roleName));
    }

    @PutMapping(value = "/my-page/profile/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "유저 프로필을 업데이트 합니다."
            , description = "유저 프로필 업데이트 API")
    public BaseResponse<UserResponse.UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UsersDetails userDetails,
            @RequestPart UserRequest.UserUpdateRequest request,
            @RequestPart MultipartFile profileImage
    ) {
        return BaseResponse.success("유저 업데이트 성공", userService.updateUser(userDetails.getUser(), request, profileImage));
    }

    @GetMapping("/my-page/qna")
    @Operation(summary = "마이페이지 고객센터 문의 목록 조회", description = "사용자가 마이페이지에서 등록한 모든 문의 내역을 조회하는 API")
    public BaseResponse<List<UserResponse.InquiryResponse>> getAllInquiry() {
        return BaseResponse.success("모든 문의 내용 조회 성공", userService.getAllInquiry());
    }

    @GetMapping("my-page/with-draw/category")
    @Operation(description="사이트에서 제공하는 정형화된 탈퇴 사유 목록을 조회합니다.")
    public BaseResponse<List<UserResponse.WithDrawReasonCategoryResponse>> getWithDrawReasonCategory() {
        return BaseResponse.success("정형화된 탈퇴 사유 조회 성공",userService.getWithDrawReasonCategory());
    }

    @DeleteMapping("my-page/with-draw")
    @Operation(description = "탈퇴 사유를 받고 유저를 탈퇴 대기상태로 만듭니다. (14일 이후 완전 탈퇴)")
    public BaseResponse<Boolean> deleteUser(@AuthenticationPrincipal UsersDetails userDetails) {
        userService.deleteUser(userDetails.getUser());
        return BaseResponse.success("회원 탈퇴 성공",true);
    }


}
