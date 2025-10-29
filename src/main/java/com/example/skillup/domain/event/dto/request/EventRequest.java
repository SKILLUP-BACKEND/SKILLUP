package com.example.skillup.domain.event.dto.request;

import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Getter
@AllArgsConstructor
public class EventRequest {
    @Getter
    @AllArgsConstructor
    public static class CreateEvent {

        @NotNull(message = "제목을 입력해주세요.")
        private String title;

        @NotNull(message = "썸네일 URL을 입력해주세요.")
        private String thumbnailUrl;

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
        private List<String> hashtags;
    }

    @Getter
    @AllArgsConstructor

    public static class UpdateEvent {
        @NotNull(message = "제목을 입력해주세요.")
        private String title;

        @NotNull(message = "썸네일 URL을 입력해주세요.")
        private String thumbnailUrl;

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

        @AssertTrue(message="sort값은 popularity, latest, deadline 만 가능합니다. ")
        public boolean isValidSort() {
            return (sort.equals("latest")||sort.equals("popularity")||sort.equals("deadline"));
        }
    }

}
