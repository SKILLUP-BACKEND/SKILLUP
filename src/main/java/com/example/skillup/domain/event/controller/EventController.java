package com.example.skillup.domain.event.controller;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.dto.response.EventResponse.EventApplyResponse;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventBookmark;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.service.EventBannerService;
import com.example.skillup.domain.event.service.EventBookmarkService;
import com.example.skillup.domain.event.service.EventService;
import com.example.skillup.domain.user.entity.UsersDetails;
import com.example.skillup.global.common.BaseResponse;
import com.example.skillup.global.interceptor.GuestIdInterceptor;
import com.example.skillup.global.search.service.EventSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "Event", description = "행사 관련 API")
public class EventController {
    private final EventService eventService;
    private final EventSearchService eventSearchService;
    private final EventBookmarkService eventBookmarkService;
    private final EventBannerService eventBannerService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //@PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "행사 등록 API", description = "관리자가 행사를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "행사 등록 성공",
            content = @Content(mediaType = "application/json"))
    public BaseResponse<EventResponse.CommonEventResponse> createEvent(
            @RequestPart("request") @Valid EventRequest.CreateEvent request,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) {
        Event event = eventService.createEvent(request, thumbnailImage);
        String message = event.getStatus() == EventStatus.DRAFT ? "행사가 임시저장 되었습니다." : "행사가 등록되었습니다.";
        return BaseResponse.success(message, new EventResponse.CommonEventResponse(event.getId()));
    }

    @PutMapping(value = "/{eventId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //@PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "행사 수정 API", description = "관리자가 특정 행사를 수정합니다.")
    public BaseResponse<EventResponse.CommonEventResponse> updateEvent(
            @PathVariable Long eventId,
            @RequestPart("request") @Valid EventRequest.UpdateEvent request,
            @RequestPart(value = "thumbnailImage", required = false) MultipartFile thumbnailImage
    ) {
        return BaseResponse.success("행사가 수정되었습니다.", eventService.updateEvent(eventId, request, thumbnailImage));
    }

    @PatchMapping("/{eventId}/visibility")
    //@PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "행사 숨김/공개 API", description = "행사의 id 와 행사의 상태의 값을 입력해주세요 입력해주신 값으로 변경됩니다. true 공개 false 숨김")
    public BaseResponse<EventResponse.CommonEventResponse> visibilityEvent(
            @PathVariable Long eventId,
            @RequestParam @NotNull boolean visibility
    ) {
        String responseText = (visibility ? "공개" : "숨김");
        return BaseResponse.success("행사가 " + responseText + " 처리되었습니다.",
                eventService.visibilityEvent(eventId, visibility));
    }

    @PatchMapping("/{eventId}/bookmarked")
    @Operation(summary = "행사를 북마크하거나 취소합니다.", description = "비회원 , admin 의 경우에는 error 처리하며 반환값으로는 북마크 여부를 반환합니다.")
    public BaseResponse<String> bookmarkEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UsersDetails user
    ) {
        EventBookmark eventBookmark = eventBookmarkService.updateBookmarked(user, eventId);
        return BaseResponse.success("북마크가 수정되었습니다.", "북마크 상태 " + eventBookmark.getIsBookmarked().toString());
    }


    @DeleteMapping("/{eventId}")
    //@PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "행사 삭제 API", description = "관리자가 특정 행사를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "행사 삭제 성공",
            content = @Content(mediaType = "application/json"))
    public BaseResponse<EventResponse.CommonEventResponse> deleteEvent(
            @PathVariable Long eventId
    ) {
        return BaseResponse.success("행사가 삭제되었습니다.", eventService.deleteEvent(eventId));
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "행사 상세 조회 API", description = "특정 행사의 상세 정보를 불러옵니다.")
    public BaseResponse<EventResponse.EventSelectResponse> getEventDetail(
            @PathVariable Long eventId,
            @AuthenticationPrincipal(errorOnInvalidType = false) UsersDetails user,
            @CookieValue(value = GuestIdInterceptor.GUEST_COOKIE_NAME, required = false) String guestId,
            HttpServletRequest request
    ) {
        String cookieGuestId = guestId;
        if (cookieGuestId == null) {
            cookieGuestId = request.getAttribute(GuestIdInterceptor.GUEST_ATTRIBUTE_NAME).toString();
        }
        EventResponse.EventSelectResponse response = eventService.getEventDetail(eventId, user, cookieGuestId);
        return BaseResponse.success("행사 상세 조회 성공", response);
    }

    @GetMapping("/home/featured")
    @Operation(summary = "추천/인기 행사 리스트", description = "진행예정/진행중 행사 중 수동 추천 또는 인기점수 상위 이벤트를 직군 탭 기준으로 반환합니다.")
    public BaseResponse<EventResponse.featuredEventResponseList> getFeaturedEvents(
            @RequestParam(defaultValue = "IT 전체") String tab,
            @RequestParam(defaultValue = "8") int size
    ) {
        return BaseResponse.success("추천/인기 행사 리스트 조회 성공", eventService.getFeaturedEvents(tab, size));
    }

    @GetMapping("/home/closing-soon")
    @Operation(
            summary = "곧 종료되는 행사 리스트",
            description = "신청 종료일까지 D-5 이하인 진행예정/진행중 + 공개 행사 중, 직군 탭과 연관된 이벤트를 인기순으로 반환합니다."
    )
    public BaseResponse<EventResponse.featuredEventResponseList> getClosingSoonEvents(
            @RequestParam(defaultValue = "8") int size,
            @AuthenticationPrincipal UsersDetails user
    ) {
        String jobGroup = (user != null && user.getUser() != null)
                ? user.getUser().getRole().getName()
                : null;

        return BaseResponse.success(
                "곧 종료되는 행사 리스트 조회 성공",
                eventService.getClosingSoonEvents(jobGroup, size)
        );
    }

    @GetMapping("/home/category")
    @Operation(summary = "카테고리별 홈 리스트", description = "부트캠프는 모집중만·인기순(동점 시 마감임박), 그 외 카테고리는 30일 이내·인기순(동점 시 마감임박) 정렬")
    public BaseResponse<EventResponse.CategoryEventResponseList> getHomeByCategory(
            @RequestParam(defaultValue = "BOOTCAMP_CLUB") EventCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return BaseResponse.success("카테고리별 리스트 조회 성공", eventService.getEventsByCategoryForHome(category, page, size));
    }

    @GetMapping("/home/admin/banners")
    //@PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "관리자용 배너 리스트 API 입니다.", description = "관리자용 배너 조회 API 입니다. 현재 배너 + 이전 배너 를 반환합니다. page 값은 1부터 넣어주세요(이전 배너용 페이지)")
    public BaseResponse<EventResponse.EventBannerAdminResponse> getBannersAll(
            @RequestParam(defaultValue = "0") int page
    ) {
        return BaseResponse.success("배너 리스트 조회 성공", eventBannerService.getEventBanners(page));
    }

    @GetMapping("/home/banners")
    //@PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "메인 페이지에 나올 배너 조회 API 입니다.", description = "현재 날짜가 배너의 노출일과 마감일 사이에 있는 배너를 반환합니다.")
    public BaseResponse<EventResponse.EventBannersResponseList> getHomeBanners(
    ) {
        return BaseResponse.success("배너 리스트 조회 성공", eventBannerService.getActiveEventBanners());
    }

    @PostMapping(value = "/home/admin/banners", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //@PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "배너 등록 API", description = "배너 등록 API 입니다. / 배너타입은 MAIN_BANNER 또는 SUB_BANNER 입니다.")
    public BaseResponse<EventResponse.EventBannerResponse> createHomeBanners(
            @RequestPart @Valid EventRequest.CreateEventBannerRequest request,
            @RequestPart("bannerImage") MultipartFile bannerImage) {
        return BaseResponse.success("배너 등록 성공", eventBannerService.createBanner(bannerImage, request));
    }

    @PatchMapping("/home/admin/banners/order")
    //@PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "배너 순서 정렬 API", description = "정렬된 상태의 배너의 ID 값을 순서대로 보내주세요 앞에 오는게 우선순위가 높습니다.")
    public BaseResponse<Void> updateDisplayOrderHomeBanners(
            @RequestBody @Valid EventRequest.BannerOrderUpdateRequest request
    ) {
        eventBannerService.updateBannerOrder(request.getBannerIds());
        return BaseResponse.success("배너 순서 수정 성공", null);
    }

    @PutMapping(value = "/home/admin/banners/{bannerId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    //@PreAuthorize("hasRole('OWNER')")
    @Operation(
            summary = "배너 수정 API",
            description = """
                    배너 수정 API 입니다.
                    - 배너 타입: MAIN_BANNER 또는 SUB_BANNER
                    - 이미지 파일(profileImage)은 선택적으로 전송 가능합니다.
                    """
    )
    public BaseResponse<EventResponse.EventBannerResponse> updateHomeBanner(
            @PathVariable Long bannerId,
            @RequestPart @Valid EventRequest.UpdateEventBannerRequest request,
            @RequestPart(name = "bannerImage", required = false) MultipartFile bannerImage
    ) {
        return BaseResponse.success("배너 수정 성공", eventBannerService.updateBanner(bannerId, request, bannerImage));
    }

    @DeleteMapping("/home/banners/{bannerId}")
    //@PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "배너 삭제 API", description = "지우실 배너 아이디를 입력해주세요")
    public BaseResponse<EventResponse.CommonBannerResponse> deleteHomeBanner(
            @PathVariable Long bannerId
    ) {
        return BaseResponse.success("배너 삭제 성공", eventBannerService.deleteBanner(bannerId));
    }

    @PostMapping("category-page/search")
    @Operation(summary = "행사 카테고리 페이지에서 검색하는 API(검색 조건이 많아 Json으로 보내기 위해서 Post 사용)", description = "특정 조건의 행사들을 불러옵니다.")
    public BaseResponse<EventResponse.SearchEventResponseList> getEventBySearch(
            @Valid @RequestBody EventRequest.EventSearchCondition condition
    ) {
        EventResponse.SearchEventResponseList response = eventService.getEventBySearch(condition);
        return BaseResponse.success("카테고리 페이지 검색 성공", response);
    }

    @GetMapping("category-page/recommended")
    @Operation(summary = "행사 카테고리 페이지 검색에서 이벤트가 부족할 때 다른 카테고리의 이벤트를 불러옵니다"
            , description = "카테고리 화면 이벤트 보충 api")
    public BaseResponse<List<EventResponse.HomeEventResponse>> getSupplementaryEvents(
            @RequestParam EventCategory category) {

        List<EventResponse.HomeEventResponse> events = eventService.getSupplementaryEvents(category);
        return BaseResponse.success("카테고리 페이지 추천 이벤트 조회 성공", events);
    }

    @GetMapping("home/recommended")
    @Operation(summary = "홈 화면에서 해쉬태그 기반으로 이벤트를 추천합니다"
            , description = "홈 화면 이벤트 추천 api")
    public BaseResponse<List<EventResponse.HomeEventResponse>> getRecommendedEvents(
            @AuthenticationPrincipal UsersDetails user) {
        List<EventResponse.HomeEventResponse> events = eventService.getRecommendedEvents(user.getUser().getId());
        return BaseResponse.success("홈 화면에서 추천 이벤트 조회 성공", events);
    }

    @GetMapping("home/recent")
    @Operation(summary = "홈 화면에서 최근 본 이벤트를 보여줍니다.",
            description = "로그인 유저는 userId 기반, 비로그인 유저는 guestId(쿠키) 기반으로 조회합니다.")
    public BaseResponse<List<EventResponse.HomeEventResponse>> getRecentEvents(
            @AuthenticationPrincipal UsersDetails user,
            @CookieValue(value = GuestIdInterceptor.GUEST_COOKIE_NAME, required = false) String guestId,
            HttpServletRequest request
    ) {
        String cookieGuestId = guestId;
        if (cookieGuestId == null) {
            cookieGuestId = request.getAttribute(GuestIdInterceptor.GUEST_ATTRIBUTE_NAME).toString();
        }
        List<EventResponse.HomeEventResponse> events =
                eventService.getRecentEvents(user != null ? String.valueOf(user.getUser().getId()) : cookieGuestId);
        return BaseResponse.success("홈 화면에서 최근 본 이벤트 조회 성공", events);
    }

    @PostMapping("/search/home")
    @Operation(summary = "행사 검색 api", description = "검색 내용의 행사들을 불러옵니다.")
    public BaseResponse<EventResponse.SearchEventResponseList> searchEvents(
            @Valid @RequestBody EventRequest.EventSearchRequest request) {
        return BaseResponse.success("검색 성공", eventSearchService.search(request));
    }

    @PatchMapping("/{eventId}/apply")
    @Operation(summary = "행사 신청 api",
            description = "행사 신청률 측정을 위한 api 입니다. 행사에서 지원버튼을 누를때를 기준으로 하며 이미 신청한 경우 증가하지 않습니다.")
    public BaseResponse<EventApplyResponse> applyEvent(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UsersDetails user,
            @CookieValue(value = GuestIdInterceptor.GUEST_COOKIE_NAME, required = false) String guestId,
            HttpServletRequest request
    ) {
        String cookieGuestId = guestId;
        if (cookieGuestId == null) {
            cookieGuestId = request.getAttribute(GuestIdInterceptor.GUEST_ATTRIBUTE_NAME).toString();
        }
        return BaseResponse.success("지원 성공", eventService.applyEvent(eventId, user, cookieGuestId));
    }

}
