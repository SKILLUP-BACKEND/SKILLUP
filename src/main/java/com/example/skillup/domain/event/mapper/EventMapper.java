package com.example.skillup.domain.event.mapper;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.enums.EventStatus;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class EventMapper {

    public Event toEntity(EventRequest.CreateEvent request) {

        return Event.builder()
                .title(request.getTitle())
                .thumbnailUrl(request.getThumbnailUrl())
                .category(request.getCategory())
                .eventStart(request.getEventStart())
                .eventEnd(request.getEventEnd())
                .recruitStart(request.getRecruitStart())
                .recruitEnd(request.getRecruitEnd())
                .isFree(request.getIsFree())
                .price(request.getPrice())
                .isOnline(request.getIsOnline())
                .locationText(request.getLocationText())
                .locationLink(request.getLocationLink())
                .applyLink(request.getApplyLink())
                .contact(request.getContact())
                .description(request.getDescription())
                .hashtags(request.getHashtags())
                .status(request.isDraft() ? EventStatus.DRAFT : EventStatus.PUBLISHED)
                .build();
    }

    public EventResponse.EventSelectResponse toEventDetailInfo(Event event) {
        return EventResponse.EventSelectResponse.builder()
                .id(event.getId())
                .title(event.getTitle())
                .thumbnailUrl(event.getThumbnailUrl())
                .category(event.getCategory())
                .eventStart(event.getEventStart())
                .eventEnd(event.getEventEnd())
                .recruitStart(event.getRecruitStart())
                .recruitEnd(event.getRecruitEnd())
                .isFree(event.getIsFree())
                .price(event.getPrice())
                .isOnline(event.getIsOnline())
                .locationText(event.getLocationText())
                .locationLink(event.getLocationLink())
                .applyLink(event.getApplyLink())
                .status(event.getStatus())
                .contact(event.getContact())
                .description(event.getDescription())
                .hashtags(event.getHashtags())
                .targetRoles(event.getTargetRoles()
                        .stream()
                        .map(TargetRole::getName)
                        .collect(Collectors.toSet()))
                .build();
    }
}
