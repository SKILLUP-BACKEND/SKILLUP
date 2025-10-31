package com.example.skillup.domain.event.controller;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.global.search.service.EventSearchService;
import com.example.skillup.domain.event.service.EventService;
import com.example.skillup.domain.user.entity.UsersDetails;
import com.example.skillup.global.common.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "Event", description = "행사 관련 API")
public class EventController {
    private final EventService eventService;
    private final EventSearchService eventSearchService;

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "행사 등록 API", description = "관리자가 행사를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "행사 등록 성공",
            content = @Content(mediaType = "application/json"))
    public BaseResponse<EventResponse.CommonEventResponse> createEvent(
            @RequestBody @Valid EventRequest.CreateEvent request
    ) {
        Event event = eventService.createEvent(request);
        String message = event.getStatus() == EventStatus.DRAFT ? "행사가 임시저장 되었습니다." : "행사가 등록되었습니다.";
        return BaseResponse.success(message ,new EventResponse.CommonEventResponse(event.getId()));
    }

    @PutMapping("/{eventId}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "행사 수정 API", description = "관리자가 특정 행사를 수정합니다.")
    public BaseResponse<EventResponse.CommonEventResponse> updateEvent(
            @PathVariable Long eventId,
            @RequestBody @Valid EventRequest.UpdateEvent request
    ) {

        return BaseResponse.success("행사가 수정되었습니다.", eventService.updateEvent(eventId,request));
    }

    @PatchMapping("/{eventId}/hide")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "행사 숨김 API", description = "관리자가 특정 행사를 숨김 처리합니다.")
    public BaseResponse<EventResponse.CommonEventResponse> hideEvent(
            @PathVariable Long eventId
    ) {

        return BaseResponse.success("행사가 숨김 처리되었습니다.", eventService.hideEvent(eventId));
    }

    @PatchMapping("/{eventId}/publish")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "행사 공개 API" , description = "관리자가 특정 행사를 공개 처리합니다.")
    public BaseResponse<EventResponse.CommonEventResponse> publishEvent(
            @PathVariable Long eventId
    ) {

        return BaseResponse.success("행사가 공개되었습니다.", eventService.publishEvent(eventId));
    }


    @DeleteMapping("/{eventId}")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "행사 삭제 API", description = "관리자가 특정 행사를 삭제합니다.")
    @ApiResponse(responseCode = "200", description = "행사 삭제 성공",
            content = @Content(mediaType = "application/json"))
    public BaseResponse<EventResponse.CommonEventResponse> deleteEvent(
            @PathVariable Long eventId
    ) {
        return BaseResponse.success("행사가 삭제되었습니다." , eventService.deleteEvent(eventId));
    }

    @GetMapping("/{eventId}")
    @Operation(summary = "행사 상세 조회 API", description = "특정 행사의 상세 정보를 불러옵니다.")
    public BaseResponse<EventResponse.EventSelectResponse> getEventDetail(
            @PathVariable Long eventId,
            @AuthenticationPrincipal UserDetails user
    ) {
        EventResponse.EventSelectResponse response = eventService.getEventDetail(eventId , user.getAuthorities());
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
                ? user.getUser().getJobGroup()
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

    @GetMapping("/home/banners")
    @Operation(summary = "메인 배너 및 서브 배너 리스트" , description = "메인 배너 순서대로 정렬 , 서브배너는 설정해둔 하나만 반환합니다.")
    public BaseResponse<EventResponse.EventBannersResponseList> getHomeBanners(){
        return BaseResponse.success("배너 리스트 조회 성공" , eventService.getEventBanners());
    }

    @PostMapping("/search")
    @Operation(summary = "행사 카테고리 페이지 검색 API(검색 조건이 많아 Json으로 보내기 위해서 Post 사용)", description="특정 조건의 행사들을 불러옵니다.")
    public BaseResponse<List<EventResponse.HomeEventResponse>> getEventBySearch(
            @Valid @RequestBody EventRequest.EventSearchCondition condition
    ){
        List<EventResponse.HomeEventResponse> response = eventService.getEventBySearch(condition);
        return BaseResponse.success("카테고리 페이지 검색 성공", response);
    }
    @GetMapping("/recommended")
    public BaseResponse<List<EventResponse.HomeEventResponse>> getRecommendedEvents(
            @RequestParam EventCategory category) {

        List<EventResponse.HomeEventResponse> events = eventService.getRecommendedEvents(category);
        return BaseResponse.success("카테고리별 추천 이벤트 조회 성공",events);
    }

    @GetMapping("/search/home")
    @Operation(summary = "행사 검색 api", description="검색 내용의 행사들을 불러옵니다.")
    public BaseResponse<EventResponse.SearchEventResponseList> searchEvents(
           @Valid @ModelAttribute EventRequest.EventSearchRequest request
    )  {
        return BaseResponse.success("검색 성공", eventSearchService.search(request));
    }

}
