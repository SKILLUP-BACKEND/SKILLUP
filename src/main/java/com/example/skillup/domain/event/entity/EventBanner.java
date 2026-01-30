package com.example.skillup.domain.event.entity;


import static jakarta.persistence.GenerationType.IDENTITY;
import static lombok.AccessLevel.PROTECTED;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.enums.BannerType;
import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = PROTECTED)
@SQLRestriction("deleted_at IS NULL")
public class EventBanner extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    public Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BannerType type;

    @Column(nullable = false , length = 512)
    public String bannerImageUrl;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false)
    @Builder.Default
    private boolean selected = true;

    @Column
    private String bannerLink;

    //화면 정렬 순서(작을수록 위로 정렬)
    @Column(nullable = false, name = "display_order")
    private int displayOrder;

    //노출 시작/종료
    @Column(nullable = false)
    private LocalDate startAt;

    @Column
    private LocalDate endAt; //null 이면 무기한


    public void updateInfo(EventRequest.UpdateEventBannerRequest request) {
        if (request.getTitle() != null) {
            this.title = request.getTitle();
        }
        if (request.getBannerLink() != null) {
            this.bannerLink = request.getBannerLink();
        }
        if (request.getBannerStart() != null) {
            this.startAt = request.getBannerStart();
        }
        if (request.getBannerEnd() != null) {
            this.endAt = request.getBannerEnd();
        }
    }

    public void updateBannerImage(String newBannerImageUrl) {
        this.bannerImageUrl = newBannerImageUrl;
    }

    public void updateBannerOrder(int displayOrder){
        this.displayOrder = displayOrder;
    }
}
