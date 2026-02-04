package com.example.skillup.domain.event.mapper;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.dto.response.EventResponse.EventBannerResponse;
import com.example.skillup.domain.event.entity.EventBanner;
import com.example.skillup.domain.event.enums.BannerType;
import com.example.skillup.global.common.CommonMapper;
import com.example.skillup.global.common.CommonResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class BannerMapper {
    public EventResponse.EventBannersResponseList toEventBannersResponseList(
            List<EventBannerResponse> mainBanner) {
        return new EventResponse.EventBannersResponseList(mainBanner);
    }

    public EventResponse.EventBannerAdminResponse toEventBannerAdminResponseList(
            List<EventBannerResponse> mainBanner , Page<EventBanner> pastBannerPages
    ){
        CommonResponse.PageInfoResponse pageInfoResponse = CommonMapper.toPageInfoResponse(pastBannerPages.getPageable() , pastBannerPages.getNumber(),(int)pastBannerPages.getTotalElements());
        List<EventBannerResponse> pastBanner = this.toEventBannerAdminResponse(pastBannerPages.getContent());
        return new EventResponse.EventBannerAdminResponse(mainBanner,pastBanner, pageInfoResponse);
    }


    public List<EventResponse.EventBannerResponse> toEventBannerAdminResponse(List<EventBanner> eventBanners) {
        return eventBanners.stream().map(eventBanner -> EventResponse.EventBannerResponse.builder()
                .id(eventBanner.getId())
                .mainTitle(eventBanner.getMainTitle())
                .displayOrder(eventBanner.getDisplayOrder())
                .startAt(eventBanner.getStartAt())
                .endAt(eventBanner.getEndAt())
                .clickCount(eventBanner.getClickCount())
                .build()).toList();
    }

    public List<EventResponse.EventBannerResponse> toEventBannerResponse(List<EventBanner> eventBanners) {
        return eventBanners.stream().map(eventBanner -> EventResponse.EventBannerResponse.builder()
                .id(eventBanner.getId())
                .mainTitle(eventBanner.getMainTitle())
                .subTitle(eventBanner.getSubTitle())
                .description(eventBanner.getDescription())
                .bannerImageUrl(eventBanner.getBannerImageUrl())
                .bannerLink(eventBanner.getBannerLink())
                .displayOrder(eventBanner.getDisplayOrder())
                .build()).toList();
    }



    public EventResponse.EventBannerResponse toCreateBannerResponse(EventBanner eventBanner) {
        return EventResponse.EventBannerResponse.builder()
                .id(eventBanner.getId())
                .mainTitle(eventBanner.getMainTitle())
                .subTitle(eventBanner.getSubTitle())
                .description(eventBanner.getDescription())
                .bannerImageUrl(eventBanner.getBannerImageUrl())
                .bannerLink(eventBanner.getBannerLink())
                .displayOrder(eventBanner.getDisplayOrder())
                .clickCount(eventBanner.getClickCount())
                .startAt(eventBanner.getStartAt())
                .endAt(eventBanner.getEndAt())
                .bannerType(eventBanner.getType().toString())
                .build();
    }

    public EventBanner toEventBanner(EventRequest.CreateEventBannerRequest request, int displayOrder,
                                     String BannerImageUrl) {
        return EventBanner.builder()
                .mainTitle(request.getMainTitle())
                .subTitle(request.getSubTitle())
                .description(request.getDescription())
                .bannerLink(request.getBannerLink())
                .displayOrder(displayOrder)
                .type(BannerType.MAIN_BANNER)
                .startAt(request.getBannerStart())
                .endAt(request.getBannerEnd())
                .bannerImageUrl(BannerImageUrl)
                .ClickCount(0L)
                .build();
    }

}
