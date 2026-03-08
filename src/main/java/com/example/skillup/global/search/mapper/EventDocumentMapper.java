package com.example.skillup.global.search.mapper;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.global.search.document.EventDocument;
import java.time.LocalDateTime;
import java.time.ZoneId;
import org.springframework.stereotype.Component;

@Component
public class EventDocumentMapper {

    public EventDocument fromEntity(Event event){

        return EventDocument.builder()
                .id(event.getId())
                .title(event.getTitle())
                .thumbnailUrl(event.getThumbnailUrl())
                .isFree(event.getIsFree())
                .price(event.getPrice())
                .isOnline(event.getIsOnline())
                .locationText(event.getLocationText())
                .recruitStart(toKst(event.getRecruitStart()))
                .recruitEnd(toKst(event.getRecruitEnd()))
                .eventStart(toKst(event.getEventStart()))
                .eventEnd(toKst(event.getEventEnd()))
                .createdAt(toKst(event.getCreatedAt()))
                .category(event.getCategory().toString())
                .build();
    }

    private static java.time.Instant toKst(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.atZone(ZoneId.of("Asia/Seoul")).toInstant();
    }
}