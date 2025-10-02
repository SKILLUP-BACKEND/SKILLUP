package com.example.skillup.domain.event.dto.response;

import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

public class EventResponse {

    @Getter
    @AllArgsConstructor
    public static class CommonEventResponse {
        private Long eventId;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class EventSelectResponse {
        private Long id;
        private String title;
        private String thumbnailUrl;
        private EventCategory category;

        private LocalDateTime eventStart;
        private LocalDateTime eventEnd;

        private LocalDateTime recruitStart;
        private LocalDateTime recruitEnd;

        private Boolean isFree;
        private Integer price;

        private Boolean isOnline;
        private String locationText;
        private String locationLink;

        private String applyLink;

        private EventStatus status;

        private String contact;

        private String description;

        private String hashtags;

        private Set<String> targetRoles;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class EventSummaryResponse {
        private Long id;
        private String title;
        private String thumbnailUrl;
        private EventCategory category;

        private LocalDateTime eventStart;
        private LocalDateTime eventEnd;

        private LocalDateTime recruitStart;
        private LocalDateTime recruitEnd;

        private Boolean isFree;
        private Integer price;

        private Boolean isOnline;
        private String locationText;
        private Set<String> targetRoles;
    }
}
