package com.example.skillup.domain.event.controller;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.service.EventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<String> createEvent(
            @RequestBody @Valid EventRequest.CreateEvent request,
            @AuthenticationPrincipal User user
    ) {
        //Event event = eventService.createEvent(userDetails.getUsers(),request);
        Event event = eventService.createEvent(request);
        String message = event.getStatus() == EventStatus.DRAFT ? "행사가 임지저장 되었습니다. " : "행사가 등록되었습니다. ";
        return ResponseEntity.ok(message + "(EVENT_ID : " + event.getId() + " " + user.getUsername() + " )");
    }
}
