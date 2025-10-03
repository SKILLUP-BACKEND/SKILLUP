package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.EventBanner;
import com.example.skillup.domain.event.enums.BannerType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EventBannerRepository extends JpaRepository<EventBanner, Long> {
    @Query("""
            select b from EventBanner b
            where b.type = :bannerType
            and b.startAt <= :now
            and (b.endAt >= :now or b.endAt is null)
            order by b.displayOrder asc
                
""")
    List<EventBanner> findActiveEventBannersByType(@Param("bannerType") BannerType bannerType, @Param("now")LocalDateTime now, Pageable pageable);
}
