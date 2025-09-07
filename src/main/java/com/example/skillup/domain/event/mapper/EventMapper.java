package com.example.skillup.domain.event.mapper;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.enums.EventStatus;
import org.springframework.stereotype.Component;

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
}
