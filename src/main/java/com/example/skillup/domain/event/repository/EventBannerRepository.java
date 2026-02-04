package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.EventBanner;
import com.example.skillup.domain.event.enums.BannerType;
import com.example.skillup.domain.event.exception.EventErrorCode;
import com.example.skillup.domain.event.exception.EventException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventBannerRepository extends JpaRepository<EventBanner, Long> {
    @Query("""
            select b from EventBanner b
            where b.type = :bannerType
            and b.startAt <= :now
            and (b.endAt >= :now or b.endAt is null)
            order by b.displayOrder asc
                
""")
    List<EventBanner> findActiveEventBannersByType(@Param("bannerType") BannerType bannerType, @Param("now") LocalDate now);

    @Query("""
        select eb
        from EventBanner eb
        where eb.type = :bannerType
          and eb.endAt < :now
        order by eb.endAt desc
    """)
    Page<EventBanner> findPastEventBannersByType(
            @Param("bannerType") BannerType bannerType,
            @Param("now") LocalDate now,
            Pageable pageable
    );

    @Query("""
        select eb
        from EventBanner eb
        where eb.type = :bannerType
          and (eb.endAt is null or eb.endAt >= :now)
        order by eb.displayOrder asc
    """)
    List<EventBanner> findCurrentAndWaitingEventBannersByType(
            @Param("bannerType") BannerType bannerType,
            @Param("now") LocalDate now
    );

    Optional<EventBanner> findTopByTypeOrderByDisplayOrderDesc(BannerType bannerType);

    List<EventBanner> findByIdIn(List<Long> bannerIds);

    default EventBanner getEventBanner(Long eventBannerId) {
        return findById(eventBannerId).orElseThrow(() -> new EventException(EventErrorCode.BANNER_ENTITY_NOT_FOUND,  "BannerID 가 " + eventBannerId + "인"));
    }
}
