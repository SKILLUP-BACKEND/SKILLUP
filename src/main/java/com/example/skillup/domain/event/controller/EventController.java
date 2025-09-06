package com.example.skillup.domain.event.controller;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.service.EventService;
import com.example.skillup.domain.user.entity.UsersDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
@Tag(name = "Event", description = "행사 관련 API")
public class EventController {
    private final EventService eventService;

    @PostMapping
    //@PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "행사 등록 API", description = "관리자가 행사를 등록합니다.")
    @ApiResponse(responseCode = "200", description = "일정 등록 성공",
            content = @Content(mediaType = "application/json"))
    public ResponseEntity<String> createEvent(
            @RequestBody @Valid EventRequest.CreateEvent request
            //@AuthenticationPrincipal UsersDetails userDetails
    ) {
        //Event event = eventService.createEvent(userDetails.getUser(),request);
        Event event = eventService.createEvent(request);
        return ResponseEntity.ok("이벤트가 등록되었습니다." + event.getId());
    }
}
