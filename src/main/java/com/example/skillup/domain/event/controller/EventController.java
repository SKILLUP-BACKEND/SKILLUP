package com.example.skillup.domain.event.controller;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.service.EventService;
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

    // 페이징 필요한지 상의 필요
    // 행사가 많이 없을 때는 모든 행사 불러오기로 행사 다 불러간 후에 프론트에서 카테고리 분류
    // 행사가 많아질 때는 해당 카테고리만 불러오기
    @GetMapping
    @Operation(summary = "행사 카테고리로 검색 API(여러가지 카테고리 선택 가능)", description = "특정 카테고리 행사들을 불러옵니다.")
    public BaseResponse<List<EventResponse.EventSelectResponse>> getEventByCategory(
            @Valid @ModelAttribute EventRequest.SearchEventByCategory category
    ) {
        List<EventResponse.EventSelectResponse> response = eventService.getEventByCategory(category);
        return BaseResponse.success("행사 상세 조회 성공", response);
    }

    @GetMapping("/all")
    @Operation(summary = "모든 행사 불러오기", description = "모든 행사들을 불러옵니다.")
    public BaseResponse<List<EventResponse.EventSelectResponse>> getAllEvent(
    ) {
        List<EventResponse.EventSelectResponse> response = eventService.getAllEvents();
        return BaseResponse.success("모든 행사 조회 성공", response);
    }
}
