package com.example.skillup.global.scheduler;

import com.example.skillup.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserCleanupScheduler
{
    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 4 * * *")
    @Transactional
    public void cleanupExpiredUsers() {
        log.info("만료된 Guest 데이터 삭제 시작");
        userRepository.deleteExpiredUsers(LocalDateTime.now().minusWeeks(2));
        log.info("만료된 Guest 데이터 삭제 완료");
    }
}
