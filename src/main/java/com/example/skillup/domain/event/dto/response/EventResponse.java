package com.example.skillup.domain.event.dto.response;

import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
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

    @Getter @Builder
    public static class HomeEventResponse {
        private Long id;
        private String thumbnailUrl;


        private boolean online;
        private String locationText;
        private String title;
        private String scheduleText;      // 형식 "2025.12.12 ~ 2025.12.31"
        private String priceText;
        private String d_dayLabel;         // "마감 D-1", "마감"

        // 태그/뱃지
        private boolean recommended;      // 추천 태그
        private boolean ad;               // 광고/제휴 태그
        private boolean bookmarked;

        private EventCategory category;
        //점수 제대로 뜨는지 확인 용
        private double recommendedRate;
    }

    @Getter @Builder
    public static class featuredEventResponseList {
        private String tab;               //  ex )"IT 전체", "기획", "디자인", "개발", "AI"
        private List<HomeEventResponse> homeEventResponseList;
    }
    @Getter @Builder
    public static class CategoryEventResponseList {
        private EventCategory category;
        private List<HomeEventResponse> homeEventResponseList;
    }
}
