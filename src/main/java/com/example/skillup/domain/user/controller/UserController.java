package com.example.skillup.domain.user.controller;


import com.example.skillup.domain.user.dto.request.UserRequest;
import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.UsersDetails;
import com.example.skillup.domain.user.service.UserService;
import com.example.skillup.global.auth.service.AuthService;
import com.example.skillup.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;

    @GetMapping()
    public BaseResponse<UserResponse.UserProfileResponse> getUsers(@AuthenticationPrincipal UsersDetails user) {
        return BaseResponse.success("유저 조회 성공"
                , userService.getUsers(user.getUser()));

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
            @Parameter(description = "조회 기준 (recruiting , closed)")
            @RequestParam String status,
            @RequestParam int page
    ) {
        return BaseResponse.success("마이페이지 북마크된 이벤트 조회 성공",
                userService.getMyPageBookMark(user.getUser(), sort, page , status));
    }

    @GetMapping("/my-page/profile/interest")
    @Operation(summary = "유저 프로필상 직무별 관심사를 불러옵니다(관심사의 유지 보수 및 정합성 관리를 위하여 DB 레벨에서 관리) "
            , description = "유저 프로필상 직무별 관심사를 가져오는 API ")
    public BaseResponse<List<UserResponse.InterestResponse>> getInterestByRole(@RequestParam String roleName) {
        return BaseResponse.success("직무별 관심사 조회 성공", userService.getInterestByRole(roleName));
    }

    @PutMapping(value = "/my-page/profile/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "유저 프로필을 업데이트 합니다."
            , description = "유저 프로필 업데이트 API")
    public BaseResponse<UserResponse.UserProfileResponse> updateProfile(
            @AuthenticationPrincipal UsersDetails userDetails,
            @RequestPart UserRequest.UserUpdateRequest request,
            @RequestPart(required = false) MultipartFile profileImage
    ) {
        return BaseResponse.success("유저 업데이트 성공", userService.updateUser(userDetails.getUser(), request, profileImage));
    }

    @GetMapping("/my-page/qna")
    @Operation(summary = "마이페이지 고객센터 문의 목록 조회", description = "사용자가 마이페이지에서 등록한 모든 문의 내역을 조회하는 API")
    public BaseResponse<List<UserResponse.InquiryResponse>> getAllInquiry() {
        return BaseResponse.success("모든 문의 내용 조회 성공", userService.getAllInquiry());
    }

    @GetMapping("my-page/with-draw/category")
    @Operation(description = "사이트에서 제공하는 정형화된 탈퇴 사유 목록을 조회합니다.")
    public BaseResponse<List<UserResponse.WithDrawReasonCategoryResponse>> getWithDrawReasonCategory() {
        return BaseResponse.success("정형화된 탈퇴 사유 조회 성공", userService.getWithDrawReasonCategory());
    }

    @DeleteMapping("my-page/with-draw")
    @Operation(description = "탈퇴 사유를 받고 유저를 탈퇴 대기상태로 만듭니다. (14일 이후 완전 탈퇴)")
    public BaseResponse<Boolean> deleteUser(
            @RequestBody UserRequest.UserWithdrawRequest request,
            @AuthenticationPrincipal UsersDetails userDetails) {
        userService.deleteUser(request, userDetails.getUser());
        return BaseResponse.success("회원 탈퇴 성공", true);
    }

    @PutMapping("/oauth/signup")
    @Operation(summary = "OAuth 첫 로그인 유저 추가정보 입력",
            description = "OAuth 최초 로그인 시 필수 추가 정보를 입력받아 정식 회원으로 전환합니다.")
    public BaseResponse<Void> completeOauthSignup(
            @RequestBody UserRequest.UserOAuthSignupRequest request,
            @AuthenticationPrincipal UsersDetails user
    ) {
        userService.completeSignup(user.getUser(), request);
        return BaseResponse.success("추가 정보 입력 성공", null);
    }

    @GetMapping("/search/recent")
    @Operation(summary = "검색 기록 조회 API", description = "최근 검색 기록 6개를 조회하는 API 입니다.")
    public BaseResponse<UserResponse.RecentSearchListResponse> getRecent(
            @AuthenticationPrincipal(errorOnInvalidType = true) UsersDetails user
    ) {
        return BaseResponse.success("최근 검색 기록 조회 성공", userService.getRecentSearches(user.getUser().getId()));
    }

    @PostMapping("/search/recent")
    @Operation(summary = "검색 기록 저장 API")
    public BaseResponse<Void> saveRecentKeyword(
            @AuthenticationPrincipal(errorOnInvalidType = true) UsersDetails user,
            @Valid @RequestBody UserRequest.SaveRecentSearchRequest request
    ) {
        userService.saveRecentSearchKeyword(user.getUser().getId(), request.getKeyword());
        return BaseResponse.success("검색 기록 저장에 성공했습니다.", null);
    }

    @DeleteMapping("/search/recent/{recentId}")
    @Operation(summary = "단일 검색 기록 삭제 API")
    public BaseResponse<Void> deleteRecentKeywordOne(
            @AuthenticationPrincipal(errorOnInvalidType = true) UsersDetails user,
            @PathVariable Long recentId
    ) {
        userService.deleteRecentSearchKeyword(user.getUser().getId(), recentId);
        return BaseResponse.success("검색 기록 삭제 성공", null);
    }

    @DeleteMapping("/search/recent/all")
    @Operation(summary = "전체 검색 기록 삭제 API")
    public BaseResponse<Void> deleteRecentKeywordAll(
            @AuthenticationPrincipal(errorOnInvalidType = true) UsersDetails user
    ) {
        userService.deleteAll(user.getUser().getId());
        return BaseResponse.success("검색 기록 전체 삭제 성공" , null);
    }

    @PostMapping("/continue-login")
    @Operation(summary = "탈퇴 대기 사용자 재로그인 및 다른 소셜 계정 로그인 처리 API",
            description = "탈퇴 대기 사용자 재로그인과 다른 소셜 계정 로그인 처리를 이 API가 다 처리하므로 만약 다른 소셜 계정 로그인이 탈퇴" +
                    "대기 상태라면 재가입으로 처리합니다")
    public BaseResponse<UserResponse.ContinueLoginResponse> continueLogin(@RequestBody UserRequest.ContinueLoginRequest request) {
        return BaseResponse.success("재 로그인 성공" , userService.continueLogin(request));
    }



}
