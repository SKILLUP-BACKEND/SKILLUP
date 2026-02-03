package com.example.skillup.domain.event.dto.response;

import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.global.common.CommonResponse;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

public class EventResponse {

    @Getter
    @AllArgsConstructor
    public static class CommonEventResponse {
        private Long eventId;
    }

    @Getter
    @AllArgsConstructor
    public static class CommonBannerResponse {
        private Long bannerId;
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
        private String locationText; //TODO : 위치 자표를 받는 거 고민
        private String locationLink; //TODO : 필요한지 고민

        private Double latitude;
        private Double longitude;

        private String applyLink;

        private EventStatus status;

        private String contact;

        private String description;

        private Set<String> hashTags;

        private boolean bookmarked;

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

    @Getter
    @Builder
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
        private Double recommendedRate;
    }

    @Getter
    @Builder
    public static class featuredEventResponseList {
        private String tab;               //  ex )"IT 전체", "기획", "디자인", "개발", "AI"
        private List<HomeEventResponse> homeEventResponseList;
    }

    @Getter
    @Builder
    public static class CategoryEventResponseList {
        private EventCategory category;
        private List<HomeEventResponse> homeEventResponseList;
    }

    @Getter
    @Builder
    public static class SearchEventResponseList {
        private int total;
        @Builder.Default
        private List<HomeEventResponse> homeEventResponseList = Collections.emptyList();
        private CommonResponse.PageInfoResponse pageInfoResponse;
        private boolean fallback;
    }

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class EventBannerResponse {
        private int displayOrder;

        private Long id;
        private String title;
        private String bannerImageUrl;
        private String bannerLink;
        private String bannerType;
        private LocalDate StartAt;
        private LocalDate EndAt;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class EventBannersResponseList {
        private List<EventBannerResponse> eventMainBannerReponseList;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class EventBannerAdminResponse {
        private List<EventBannerResponse> eventActiveBannerList;
        private List<EventBannerResponse> eventPastBannerList;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    public static class EventApplyResponse {
        private Long eventId;
        private String comment;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AdminEventPageResponse {
        //통계
        private AdminEventSummary summary;
        // 카테고리 카운트
        private List<AdminCategoryCount> categoryCounts;

        private List<AdminEventRow> events;
        private CommonResponse.PageInfoResponse pageInfoResponse;
    }


    @Getter
    @Builder
    @AllArgsConstructor
    public static class AdminEventSummary {
        private long totalRegisteredCount;
        private long recruitingScheduledCount;  // 모집예정
        private long recruitingCount;           // 모집중
        private long recruitingClosedCount;     // 모집마감
        private long ongoingCount;
        private Long creatableCount;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AdminCategoryCount {
        private String category;
        private long count;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class AdminEventRow {
        private Long id;

        private Long no;

        private String title;
        private String category;

        private String eventPeriodText;
        private LocalDate eventRecruitEnd;

        private Long viewsCount;
        private Long bookmarksCount;

        private String status;

        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class AdminDraftEventResponse {
        List<AdminEventRow> draftEventRowList;
        Long totalEventCount;
    }
}
