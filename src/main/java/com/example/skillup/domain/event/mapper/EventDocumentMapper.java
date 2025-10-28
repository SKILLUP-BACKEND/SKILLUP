package com.example.skillup.domain.event.mapper;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.search.document.EventDocument;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
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
                .recruitStart(toUtc(event.getRecruitStart()))
                .recruitEnd(toUtc(event.getRecruitEnd()))
                .eventStart(toUtc(event.getEventStart()))
                .eventEnd(toUtc(event.getEventEnd()))
                .category(event.getCategory().toString())
                .build();
    }

    private static java.time.Instant toUtc(LocalDateTime localDateTime) {
        return localDateTime == null ? null : localDateTime.toInstant(ZoneOffset.UTC);
    }
}
