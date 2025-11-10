package com.example.skillup.domain.event.mapper;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventBanner;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.global.search.document.EventDocument;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

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

    public EventResponse.EventSelectResponse toEventDetailInfo(Event event , boolean isBookmarked) {
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
                .bookmarked(isBookmarked)
                .targetRoles(event.getTargetRoles()
                        .stream()
                        .map(TargetRole::getName)
                        .collect(Collectors.toSet()))
                .build();
    }

    public EventResponse.HomeEventResponse toFeaturedEvent(Event event, boolean bookmarked, boolean recommended,
                                                           boolean ad, double score) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        String schedule = formatRange(event.getEventStart(), event.getEventEnd(), fmt);
        String priceText = event.getIsFree() != null && event.getIsFree()
                ? "무료"
                : (event.getPrice() != null
                        ? NumberFormat.getNumberInstance(Locale.KOREA).format(event.getPrice()) + "₩"
                        : null);

        String d_day = calcDdayLabel(event.getRecruitEnd());

        return EventResponse.HomeEventResponse.builder()
                .id(event.getId())
                .thumbnailUrl(event.getThumbnailUrl())
                .online(Boolean.TRUE.equals(event.getIsOnline()))
                .locationText(event.getLocationText())
                .title(event.getTitle())
                .scheduleText(schedule)
                .priceText(priceText)
                .d_dayLabel(d_day)
                .recommended(recommended)
                .ad(ad)
                .bookmarked(bookmarked)
                .category(event.getCategory())
                .recommendedRate(score)
                .build();
    }

    public EventResponse.featuredEventResponseList toFeaturedEventResponseList(
            List<EventResponse.HomeEventResponse> events, String tab) {
        return EventResponse.featuredEventResponseList.builder()
                .homeEventResponseList(events)
                .tab(tab == null ? "IT 전체" : tab)
                .build();
    }

    public EventResponse.CategoryEventResponseList toCategoryEventResponseList(
            List<EventResponse.HomeEventResponse> events, EventCategory category) {
        return EventResponse.CategoryEventResponseList.builder()
                .category(category)
                .homeEventResponseList(events)
                .build();
    }

    public EventResponse.EventBannersResponseList toEventBannersResponseList(
            List<EventResponse.EventBannerResponse> mainBanner, List<EventResponse.EventBannerResponse> subBanner) {
        return new EventResponse.EventBannersResponseList(mainBanner, subBanner);
    }

    public List<EventResponse.EventBannerResponse> toEventBannerResponse(List<EventBanner> eventBanners) {
        return eventBanners.stream().map(eventBanner -> EventResponse.EventBannerResponse.builder()
                .title(eventBanner.getTitle())
                .bannerImageUrl(eventBanner.getBannerImageUrl())
                .bannerLink(eventBanner.getBannerLink())
                .displayOrder(eventBanner.getDisplayOrder())
                .StartAt(eventBanner.getStartAt())
                .EndAt(eventBanner.getEndAt())
                .build()).toList();
    }

    public EventResponse.HomeEventResponse mapEsDocToHomeItem(
            EventDocument eventDocument,
            Double score
    ) {
        LocalDateTime startDt = eventDocument.getEventStart() == null
                ? null
                : LocalDateTime.ofInstant(eventDocument.getEventStart(), KST);

        LocalDateTime endDt = eventDocument.getEventEnd() == null
                ? null
                : LocalDateTime.ofInstant(eventDocument.getEventEnd(), KST);

        String schedule = formatRange(startDt, endDt, DATE_FMT);

        String priceText = Boolean.TRUE.equals(eventDocument.getIsFree())
                ? "무료"
                : (eventDocument.getPrice() != null
                        ? NumberFormat.getNumberInstance(Locale.KOREA).format(eventDocument.getPrice()) + "₩"
                        : null);

        LocalDateTime recruitEndDt = eventDocument.getRecruitEnd() == null
                ? null
                : LocalDateTime.ofInstant(eventDocument.getRecruitEnd(), KST);

        String d_day = calcDdayLabel(recruitEndDt);

        EventCategory category = null;
        if (eventDocument.getCategory() != null) {
            try {
                category = EventCategory.valueOf(eventDocument.getCategory());
            } catch (IllegalArgumentException ignore) {
            }
        }

        double recommendedRate = score == null ? 0.0 : score;

        return EventResponse.HomeEventResponse.builder()
                .id(eventDocument.getId())
                .thumbnailUrl(eventDocument.getThumbnailUrl())
                .online(Boolean.TRUE.equals(eventDocument.getIsOnline()))
                .locationText(eventDocument.getLocationText())
                .title(eventDocument.getTitle())
                .scheduleText(schedule)
                .priceText(priceText)
                .d_dayLabel(d_day)
                .recommended(Boolean.TRUE.equals(eventDocument.getRecommendedManual()))
                .ad(Boolean.TRUE.equals(eventDocument.getAd()))
                .bookmarked(false)
                .category(category)
                .recommendedRate(recommendedRate)
                .build();
    }

    public EventResponse.SearchEventResponseList toSearchEventResponseList
            (int total, List<EventResponse.HomeEventResponse> events) {
        return EventResponse.SearchEventResponseList.builder()
                .total(total)
                .homeEventResponseList(events)
                .build();
    }

    private String formatRange(LocalDateTime start, LocalDateTime end, DateTimeFormatter fmt) {
        if (start == null && end == null) {
            return null;
        }
        if (start != null && end != null) {
            return start.format(fmt) + " ~ " + end.format(fmt);
        }
        return (start != null) ? start.format(fmt) : end.format(fmt);
    }

    private String calcDdayLabel(LocalDateTime recruitEnd) {
        if (recruitEnd == null) {
            return null;
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(recruitEnd)) {
            return "마감";
        }
        long days = java.time.Duration.between(now.toLocalDate().atStartOfDay(),
                recruitEnd.toLocalDate().atStartOfDay()).toDays();
        if (days <= 0) {
            return "마감 D-0";
        }
        return "마감 D-" + days;
    }

}
