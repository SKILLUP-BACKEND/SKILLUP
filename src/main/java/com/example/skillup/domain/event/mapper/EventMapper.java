package com.example.skillup.domain.event.mapper;

import static com.example.skillup.global.common.CommonMapper.toBigDecimal;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.dto.response.EventResponse.AdminCategoryCount;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.HashTag;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.enums.AdminEventListStatus;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.repository.EventRepository.AdminCategoryCountProjection;
import com.example.skillup.domain.event.repository.EventRepository.AdminEventSummaryProjection;
import com.example.skillup.domain.event.repository.EventRepositoryImpl;
import com.example.skillup.domain.map.provider.GeocodingProvider.GeoPoint;
import com.example.skillup.global.common.CommonMapper;
import com.example.skillup.global.common.CommonResponse.PageInfoResponse;
import com.example.skillup.global.search.document.EventDocument;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy.MM.dd");

    public Event toEntity(EventRequest.CreateEvent request, String thumbnailUrl, GeoPoint geoPoint) {

        BigDecimal lat = (geoPoint == null) ? null : toBigDecimal(geoPoint.lat());
        BigDecimal lng = (geoPoint == null) ? null : toBigDecimal(geoPoint.lng());

        return Event.builder()
                .title(request.getTitle())
                .thumbnailUrl(thumbnailUrl)
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
                .status(EventStatus.PUBLISHED)
                .latitude(lat)
                .longitude(lng)
                .build();
    }

    public Event toDraftEntity(EventRequest.CreateDraftEvent request, String thumbnailUrl) {
        return Event.builder()
                .title(request.getTitle())
                .thumbnailUrl(thumbnailUrl)
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
                .status(EventStatus.DRAFT)
                .build();
    }

    public EventResponse.EventSelectResponse toEventDetailInfo(Event event, boolean isBookmarked) {
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
                .bookmarked(isBookmarked)
                .hashTags(event.getHashTags()
                        .stream()
                        .map(HashTag::getName)
                        .collect(Collectors.toSet()))
                .targetRoles(event.getTargetRoles()
                        .stream()
                        .map(TargetRole::getName)
                        .collect(Collectors.toSet()))
                .longitude((event.getLongitude() == null) ? null : event.getLongitude().doubleValue())
                .latitude((event.getLatitude() == null) ? null : event.getLatitude().doubleValue())
                .build();
    }

    public EventResponse.HomeEventResponse toFeaturedEvent(Event event, boolean bookmarked, boolean recommended,
                                                           boolean ad, Double score) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        String schedule = formatRange(event.getEventStart(), event.getEventEnd(), fmt);
        String priceText = event.getIsFree() != null && event.getIsFree()
                ? "무료"
                : (event.getPrice() != null
                        ? NumberFormat.getNumberInstance(Locale.KOREA).format(event.getPrice()) + "원"
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
                .tab(tab)
                .build();
    }

    public EventResponse.CategoryEventResponseList toCategoryEventResponseList(
            List<EventResponse.HomeEventResponse> events, EventCategory category) {
        return EventResponse.CategoryEventResponseList.builder()
                .category(category)
                .homeEventResponseList(events)
                .build();
    }

    public EventResponse.HomeEventResponse mapEsDocToHomeItem(
            EventDocument eventDocument,
            Double score,
            boolean isBookmarked
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
                .bookmarked(isBookmarked)
                .category(category)
                .recommendedRate(recommendedRate)
                .build();
    }

    public EventResponse.SearchEventResponseList toSearchEventResponseList
            (int total, List<EventResponse.HomeEventResponse> events, boolean fallback) {
        return EventResponse.SearchEventResponseList.builder()
                .total(total)
                .homeEventResponseList(events)
                .fallback(fallback)
                .build();
    }

    public EventResponse.SearchEventResponseList toCategoryPageEventResponseListWithPageable(
            List<EventRepositoryImpl.EventWithPopularity> events,
            Pageable pageable,
            int page,
            int totalCount,
            Set<Long> bookmarkedEventIds
    ) {
        return EventResponse.SearchEventResponseList.builder()
                .homeEventResponseList(
                        events.stream()
                                .map(r -> {
                                    Event event = r.getEvent();
                                    double score = r.getPopularity();
                                    boolean bookmarked = bookmarkedEventIds.contains(event.getId());
                                    return toFeaturedEvent(
                                            event,
                                            bookmarked,
                                            event.isRecommendedManual(),
                                            event.isAd(),
                                            score
                                    );
                                })
                                .toList()
                )
                .total(totalCount)
                .pageInfoResponse(
                        CommonMapper.toPageInfoResponse(pageable, page, totalCount)
                )
                .build();
    }

    public List<EventResponse.HomeEventResponse> toHomeEventResponsList(List<Event> events,
                                                                        Set<Long> bookmarkedEventIds) {
        return events.stream()
                .map(event -> {
                            boolean bookmarked = bookmarkedEventIds.contains(event.getId());
                            return toFeaturedEvent(
                                    event,
                                    bookmarked,
                                    event.isRecommendedManual(),
                                    event.isAd(),
                                    null
                            );
                        }
                )
                .toList();
    }

    public EventResponse.EventHashTagResponse toEventHashTagResponse(List<Event> events , Set<Long> bookmarkedEventIds , List<HashTag> hashTags) {
        List<EventResponse.HomeEventResponse> eventResponses = toHomeEventResponsList(events, bookmarkedEventIds);

        return EventResponse.EventHashTagResponse.builder()
                .events(eventResponses)
                .hashTags(hashTags.stream().map(HashTag::getName).toList())
                .build();
    }

    public List<EventResponse.HomeEventResponse> toCategoryPageEventResponseList(
            List<EventRepositoryImpl.EventWithPopularity> events,
            Set<Long> bookmarkedEventIds
    ) {
        return events.stream()
                .map(r -> {
                    Event event = r.getEvent();
                    boolean bookmarked = bookmarkedEventIds.contains(event.getId());
                    return toFeaturedEvent(
                            event,
                            bookmarked,
                            event.isRecommendedManual(),
                            event.isAd(),
                            r.getPopularity()
                    );
                })
                .toList();
    }

    public List<EventResponse.AdminEventRow> toAdminEventRowList(List<Event> events, int page, int size,
                                                                 long totalCount, LocalDateTime now) {

        int startIndex = page * size;

        return IntStream.range(0, events.size()).mapToObj(
                idx -> {
                    Event event = events.get(idx);
                    long no = totalCount - (startIndex + idx);

                    AdminEventListStatus status = resolveAdminListStatus(event, now);

                    return EventResponse.AdminEventRow.builder()
                            .id(event.getId())
                            .no(no)
                            .title(event.getTitle())
                            .category(event.getCategory().getToKorean())
                            .eventPeriodText(formatRange(event.getEventStart(), event.getEventEnd(), DATE_FMT))
                            .viewsCount(event.getViewsCount())
                            .bookmarksCount(event.getBookmarkedCount())
                            .status(status.getToKorean())
                            .createdAt(event.getCreatedAt())
                            .build();
                }
        ).toList();
    }



    public EventResponse.AdminDraftEventResponse toAdminDraftEventRowList(List<Event> events){

        List<EventResponse.AdminEventRow> eventRow = IntStream.range(0,events.size()).mapToObj(
                idx -> {
                    Event event = events.get(idx);
                    long no = events.size() - idx;

                    return EventResponse.AdminEventRow.builder()
                            .id(event.getId())
                            .no(no)
                            .title(event.getTitle())
                            .eventRecruitEnd(event.getRecruitEnd() != null ? event.getRecruitEnd().toLocalDate() : null)
                            .eventPeriodText(formatRange(event.getEventStart(), event.getEventEnd(), DATE_FMT))
                            .createdAt(event.getCreatedAt())
                            .build();
                }
        ).toList();

        return new EventResponse.AdminDraftEventResponse(eventRow , (long)events.size());
    }

    public EventResponse.AdminEventPageResponse toAdminEventPageResponse(List<EventResponse.AdminEventRow> eventRowList,
                                                                         AdminEventSummaryProjection countSummary,
                                                                         List<AdminCategoryCountProjection> categoryCount,
                                                                         PageInfoResponse pageInfoResponse) {

        EventResponse.AdminEventSummary adminEventSummary = resolveAdminListStatus(countSummary);
        List<EventResponse.AdminCategoryCount> adminCategoryCountList = resolveAdminCategory(categoryCount);

        return EventResponse.AdminEventPageResponse.builder()
                .events(eventRowList)
                .summary(adminEventSummary)
                .categoryCounts(adminCategoryCountList)
                .pageInfoResponse(pageInfoResponse)
                .build();
    }

    private List<AdminCategoryCount> resolveAdminCategory(List<AdminCategoryCountProjection> categoryCount){
        List<AdminCategoryCount> adminCategoryCountList = new ArrayList<>();

        long totalCount = 0;

        for (AdminCategoryCountProjection countProjection : categoryCount) {

            adminCategoryCountList.add(
                    AdminCategoryCount.builder().category(countProjection.getCategory().getToKorean())
                            .count(countProjection.getCount()).build());

            totalCount += countProjection.getCount();
        }

        adminCategoryCountList.add(AdminCategoryCount.builder()
                .category(EventCategory.ALL.getToKorean())
                .count(totalCount)
                .build());

        return adminCategoryCountList;
    }

    private EventResponse.AdminEventSummary resolveAdminListStatus(AdminEventSummaryProjection countSummary) {
        return EventResponse.AdminEventSummary.builder()
                .totalRegisteredCount(countSummary.getTotalRegistered())
                .recruitingScheduledCount(countSummary.getRecruitingScheduled())
                .recruitingCount(countSummary.getRecruiting())
                .recruitingClosedCount(countSummary.getRecruitingClosed())
                .ongoingCount(countSummary.getOngoing())
                .creatableCount(countSummary.getCreatableCount())
                .build();
    }

    public EventResponse.EventApplyResponse toEventApplyResponse(Long eventId) {
        return EventResponse.EventApplyResponse.builder()
                .eventId(eventId)
                .comment("성공적으로 신청되었습니다.")
                .build();
    }


    private AdminEventListStatus resolveAdminListStatus(Event e, LocalDateTime now) {
        LocalDateTime recruitStart = e.getRecruitStart();
        LocalDateTime recruitEnd = e.getRecruitEnd();
        LocalDateTime eventEnd = e.getEventEnd();

        if (eventEnd.isBefore(now)) {
            return AdminEventListStatus.ENDED;
        }
        if (now.isBefore(recruitStart)) {
            return AdminEventListStatus.RECRUIT_SCHEDULED;
        }

        if (!now.isBefore(recruitStart) && !now.isAfter(recruitEnd)) {
            return AdminEventListStatus.RECRUITING;
        }
        if (now.isAfter(recruitEnd)) {
            return AdminEventListStatus.RECRUIT_CLOSED;
        }

        return AdminEventListStatus.RECRUITING; //예외 상황
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
            return "신청 마감";
        }
        long days = java.time.Duration.between(now.toLocalDate().atStartOfDay(),
                recruitEnd.toLocalDate().atStartOfDay()).toDays();
        if (days <= 0) {
            return "마감 D-0";
        }
        return "마감 D-" + days;
    }

}
