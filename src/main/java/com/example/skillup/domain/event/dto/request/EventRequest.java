package com.example.skillup.domain.event.dto.request;

import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

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

        private String hashtags;
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

        private String hashtags;
    }

    @Getter
    @AllArgsConstructor
    public static class SearchEventByCategory {

        @NotNull(message = "카테고리는 필수입니다.")
        private List<EventCategory> categories;
    }
}
