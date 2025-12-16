package com.example.skillup.global.scheduler;

import com.example.skillup.domain.user.repository.GuestRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuestCleanupScheduler {

    private final GuestRepository guestRepository;

    @Scheduled(cron = "0 0 4 * * *")//새벽 4시로 설정했습니다!
    @Transactional
    public void cleanupExpiredGuests() {
        log.info("만료된 Guest 데이터 삭제 시작");
        guestRepository.deleteExpiredGuests(LocalDateTime.now());
        log.info("만료된 Guest 데이터 삭제 완료");
    }

}
