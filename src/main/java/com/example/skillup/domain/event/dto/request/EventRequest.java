package com.example.skillup.domain.event.dto.request;

import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventFormat;
import com.example.skillup.domain.event.enums.EventSortType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@AllArgsConstructor
public class EventRequest {
    @Getter
    @AllArgsConstructor
    public static class CreateEvent {

        @NotNull(message = "제목을 입력해주세요.")
        private String title;

        //@NotNull(message = "썸네일 URL을 입력해주세요.")
        //private String thumbnailUrl;

        @NotNull(message = "카테고리를 선택해주세요.")
        private EventCategory category;


        @NotNull(message = "행사 시작일을 입력해주세요.")
        private LocalDateTime eventStart;
        private LocalDateTime eventEnd;


        @NotNull(message = "모집 시작일을 입력해주세요.")
        private LocalDateTime recruitStart;
        private LocalDateTime recruitEnd;


        @NotNull(message = "참가비 정보를 입력해주세요.")
        private Boolean isFree;
        private Integer price;

        @NotNull(message = "추천 대상 최소 1개 선택해주세요.")
        @Size(min = 1, message = "최소 1개의 추천 대상이 필요합니다.")
        private List<String> targetRoles;

        @NotNull(message = "임시저장인지 등록인지 값을 보내주세요")
        private boolean draft; // true 임시저장, false 최종등록

        private Boolean isOnline;

        private String locationText;
        private String locationLink;

        private String applyLink;

        private String contact;

        private String description;

        @Size(min = 1, max = 5, message = "해시태그는 1개 이상 5개 이하로 선택해주세요.")
        private List<String> hashTags;
    }

    @Getter
    @AllArgsConstructor
    public static class UpdateEvent {
        @NotNull(message = "제목을 입력해주세요.")
        private String title;

        //@NotNull(message = "썸네일 URL을 입력해주세요.")
        //private String thumbnailUrl;

        @NotNull(message = "카테고리를 선택해주세요.")
        private EventCategory category;


        @NotNull(message = "행사 시작일을 입력해주세요.")
        private LocalDateTime eventStart;
        private LocalDateTime eventEnd;


        @NotNull(message = "모집 시작일을 입력해주세요.")
        private LocalDateTime recruitStart;
        private LocalDateTime recruitEnd;


        @NotNull(message = "참가비 정보를 입력해주세요.")
        private Boolean isFree;
        private Integer price;

        @NotNull(message = "추천 대상 최소 1개 선택해주세요.")
        @Size(min = 1, message = "최소 1개의 추천 대상이 필요합니다.")
        private List<String> targetRoles;

        @NotNull(message = "임시저장인지 등록인지 값을 보내주세요")
        private boolean draft; // true 임시저장, false 최종등록

        private Boolean isOnline;

        private String locationText;
        private String locationLink;

        private String applyLink;

        private String contact;

        private String description;
        @Size(min = 1, max = 5, message = "해시태그는 1개 이상 5개 이하로 선택해주세요.")
        private List<String> hashTags;
    }

    @Getter
    @AllArgsConstructor
    @Builder
    public static class EventSearchCondition {
        @NotNull(message = "카테고리를 선택해주세요.")
        private EventCategory category;

        private Boolean isOnline;

        private Boolean isFree;

        private LocalDateTime startDate;

        private LocalDateTime endDate;

        @NotNull(message = "정렬 기준을 선택해주세요. (기본 값은 인기순)")
        private String sort;

        private List<String> targetRoles;

        @NotNull(message = "페이지 번호를 입력해주세요. (페이지당 게시글은 12개)")
        private int page;

        @AssertTrue(message = "시작일이 있으면 종료일도 함께 입력해야 합니다.")
        public boolean isValidDateRange() {
            return (startDate == null && endDate == null)
                    || (startDate != null && endDate != null);
        }

        @AssertTrue(message = "sort값은 popularity, latest, deadline 만 가능합니다. ")
        public boolean isValidSort() {
            return (sort.equals("latest") || sort.equals("popularity") || sort.equals("deadline"));
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class EventSearchRequest {

        @NotNull(message = "검색어를 입력해주세요")
        private String searchString;

        private EventSortType sort = EventSortType.POPULARITY;

        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private OffsetDateTime eventStart;
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        private OffsetDateTime eventEnd;


        private EventFormat eventFormat;

        private Boolean isFree;

        @NotNull(message = "페이지 번호를 입력해주세요. (페이지당 게시글은 12개)")
        private Integer page = 0;

    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class CreateEventBannerRequest {

        @NotBlank(message = "배너명을 입력해주세요")
        @Size(max = 100)
        private String title;

        @NotNull(message = "배너 클릭시 이동 할 링크를 입력해주세요")
        private String bannerLink;

        @NotNull(message = "배너 노출 시작일을 입력해주세요")
        @FutureOrPresent(message = "배너 노출 시작일은 오늘 이전일 수 없습니다.")
        LocalDate bannerStart;

        @NotNull(message = "배너 노출 마감일을 입력해주세요")
        LocalDate bannerEnd;

        @AssertTrue(message = "배너 종료일은 시작일보다 빠를 수 없습니다.")
        @JsonIgnore
        public boolean isValidPeriod() {
            return !bannerEnd.isBefore(bannerStart);
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class UpdateEventBannerRequest {

        @NotBlank(message = "배너명을 입력해주세요")
        @Size(max = 100)
        private String title;

        @NotNull
        private String bannerLink;

        @NotNull(message = "배너 노출 시작일은 필수입니다.")
        @FutureOrPresent
        private LocalDate bannerStart;

        private LocalDate bannerEnd;

        @AssertTrue(message = "배너 종료일은 시작일보다 빠를 수 없습니다.")
        @JsonIgnore
        public boolean isValidPeriod() {
            return !bannerEnd.isBefore(bannerStart);
        }
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class BannerOrderUpdateRequest{
        @NotEmpty List<Long> bannerIds;
    }

}
