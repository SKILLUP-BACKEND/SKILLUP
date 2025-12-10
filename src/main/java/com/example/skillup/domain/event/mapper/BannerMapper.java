package com.example.skillup.domain.event.mapper;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.dto.response.EventResponse.EventBannerResponse;
import com.example.skillup.domain.event.entity.EventBanner;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class BannerMapper {
    public EventResponse.EventBannersResponseList toEventBannersResponseList(
            List<EventBannerResponse> mainBanner, List<EventResponse.EventBannerResponse> subBanner) {
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

    public EventResponse.EventBannerResponse toCreateBannerResponse(EventBanner eventBanner) {
        return EventResponse.EventBannerResponse.builder()
                .title(eventBanner.getTitle())
                .bannerImageUrl(eventBanner.getBannerImageUrl())
                .bannerLink(eventBanner.getBannerLink())
                .displayOrder(eventBanner.getDisplayOrder())
                .StartAt(eventBanner.getStartAt())
                .EndAt(eventBanner.getEndAt())
                .bannerType(eventBanner.getType().toString())
                .build();
    }

    public EventBanner toEventBanner(EventRequest.CreateEventBannerRequest request , int displayOrder , String BannerImageUrl) {
        return EventBanner.builder()
                .title(request.getTitle())
                .bannerLink(request.getBannerLink())
                .displayOrder(displayOrder)
                .type(request.getBannerType())
                .startAt(request.getBannerStart())
                .endAt(request.getBannerEnd())
                .bannerImageUrl(BannerImageUrl)
                .build();
    }

}
