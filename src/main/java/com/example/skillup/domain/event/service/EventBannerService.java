package com.example.skillup.domain.event.service;


import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.EventBanner;
import com.example.skillup.domain.event.enums.BannerType;
import com.example.skillup.domain.event.exception.EventErrorCode;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.mapper.BannerMapper;
import com.example.skillup.domain.event.repository.EventBannerRepository;
import com.example.skillup.global.service.S3Service;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class EventBannerService {

    private final EventBannerRepository eventBannerRepository;
    private final BannerMapper bannerMapper;
    private final S3Service s3Service;

    LocalDateTime now = LocalDateTime.now();

    @Transactional
    public EventResponse.EventBannerResponse createBanner(MultipartFile Banner,
                                                          EventRequest.CreateEventBannerRequest request) {

        String bannerSavePath = "banner/" + (request.getBannerType() == BannerType.MAIN_BANNER ? "main" : "sub");
        int displayOrder = eventBannerRepository.findTopByTypeOrderByDisplayOrderDesc(request.getBannerType())
                .map(EventBanner::getDisplayOrder).map(o -> o + 1).orElse(1);
        String bannerUrl = s3Service.uploadFile(Banner, bannerSavePath);
        EventBanner eventBanner = bannerMapper.toEventBanner(request, displayOrder, bannerUrl);

        return bannerMapper.toCreateBannerResponse(eventBannerRepository.save(eventBanner));
    }

    @Transactional(readOnly = true)
    public EventResponse.EventBannersResponseList getEventBanners() {

        List<EventBanner> mainBanners = eventBannerRepository.findActiveEventBannersByType(BannerType.MAIN_BANNER, now,
                PageRequest.of(0, 5));
        List<EventBanner> subBanner = eventBannerRepository.findActiveEventBannersByType(BannerType.SUB_BANNER, now,
                PageRequest.of(0, 1));

        List<EventResponse.EventBannerResponse> mainEventBanners = bannerMapper.toEventBannerResponse(mainBanners);
        List<EventResponse.EventBannerResponse> subEventBanners = bannerMapper.toEventBannerResponse(subBanner);

        return bannerMapper.toEventBannersResponseList(mainEventBanners, subEventBanners);

    }

    @Transactional
    public EventResponse.EventBannerResponse updateBanner(Long bannerId , EventRequest.UpdateEventBannerRequest request,
                                                          MultipartFile bannerImage) {
        EventBanner banner = eventBannerRepository.findById(bannerId)
                .orElseThrow(() -> new EventException(EventErrorCode.BANNER_ENTITY_NOT_FOUND));

        if (bannerImage != null && !bannerImage.isEmpty()) {
            String bannerSavePath = "banner/" + (request.getBannerType() == BannerType.MAIN_BANNER ? "main" : "sub");
            String BannerUrl = s3Service.uploadFile(bannerImage, bannerSavePath);
            banner.updateBannerImage(BannerUrl);
        }

        banner.updateInfo(request);

        return bannerMapper.toCreateBannerResponse(banner);
    }

    @Transactional
    public EventResponse.CommonBannerResponse deleteBanner(Long bannerId) {
        EventBanner banner = eventBannerRepository.findById(bannerId)
                .orElseThrow(() -> new EventException(EventErrorCode.BANNER_ENTITY_NOT_FOUND));

        banner.delete();

        return new EventResponse.CommonBannerResponse(banner.getId());
    }

}
