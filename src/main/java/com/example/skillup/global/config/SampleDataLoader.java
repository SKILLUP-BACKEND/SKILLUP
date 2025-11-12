package com.example.skillup.global.config;

import com.example.skillup.domain.admin.entity.Admin;
import com.example.skillup.domain.admin.enums.AdminRole;
import com.example.skillup.domain.admin.repository.AdminRepository;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventLike;
import com.example.skillup.domain.event.entity.EventViewDaily;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.repository.EventLikeRepository;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.event.repository.EventViewDailyRepository;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.enums.UserStatus;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.search.entity.SynonymGroup;
import com.example.skillup.global.search.entity.SynonymTerm;
import com.example.skillup.global.search.repository.SynonymGroupRepository;
import com.example.skillup.global.search.repository.SynonymTermRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@Profile("local-seed") // 실행 프로필: local-seed 일 때만 동작
@RequiredArgsConstructor
public class SampleDataLoader implements CommandLineRunner {

    private final EventRepository eventRepository;
    private final TargetRoleRepository targetRoleRepository;
    private final EventViewDailyRepository eventViewDailyRepository;
    private final EventLikeRepository eventLikeRepository;
    private final UserRepository usersRepository; // 있으면 사용
    private final AdminRepository adminRepository;
    private final SynonymGroupRepository synonymGroupRepository;
    private final SynonymTermRepository synonymTermRepository;

    @Override
    @Transactional
    public void run(String... args) {

        TargetRole dev = getOrCreateRole("개발자");
        TargetRole design = getOrCreateRole("디자이너");
        TargetRole planner = getOrCreateRole("기획자");

        targetRoleRepository.saveAll(List.of(dev, design, planner));

        LocalDateTime now = LocalDateTime.now();

        // 2) 이벤트 4종 생성
        // A. 부트캠프(모집중)
        Event bootcampOpen = Event.builder()
                .title("🔥 실무형 백엔드 부트캠프")
                .category(EventCategory.BOOTCAMP_CLUB)
                .status(EventStatus.PUBLISHED)

                .recruitStart(now.minusDays(10))
                .recruitEnd(now.plusDays(10))
                .eventStart(now.plusDays(20))
                .eventEnd(now.plusDays(40))

                .isOnline(false)
                .isFree(true)
                .price(0)
                .thumbnailUrl("https://example.com/thumb/backend.jpg")
                .applyClicks(120L)
                .viewsCount(0L)
                .likesCount(0L)

                .applyLink("https://example.com/apply/backend")
                .locationText("서울 강남")
                .locationLink("https://maps.example.com/abc")
                .contact("admin@example.com")
                .recommendedManual(false)
                .ad(false)
                .description("실무형 백엔드 집중 과정")
                .build();
        bootcampOpen.addTargetRole(dev);

// B. 해커톤(30일 이내)
        Event hackathon = Event.builder()
                .title("🚀 AI 해커톤 2025")
                .category(EventCategory.COMPETITION_HACKATHON)
                .status(EventStatus.PUBLISHED)
                .recruitStart(now.minusDays(3))
                .recruitEnd(now.plusDays(7))
                .eventStart(now.plusDays(10))
                .eventEnd(now.plusDays(20))
                .isOnline(true)
                .isFree(true)
                .price(0)
                .thumbnailUrl("https://example.com/thumb/hack.jpg")
                .applyClicks(60L)
                .viewsCount(0L)
                .likesCount(0L)
                .applyLink("https://example.com/apply/hack")
                .locationText("온라인")
                .contact("hack@example.com")
                .recommendedManual(false)
                .ad(false)
                .description("인공지능 주제 해커톤")
                .build();
        hackathon.addTargetRole(dev);
        hackathon.addTargetRole(planner);

// C. 세미나(30일 초과 — 필터로 제외될 예정)
        Event seminarLate = Event.builder()
                .title("클라우드 세미나")
                .category(EventCategory.CONFERENCE_SEMINAR)
                .status(EventStatus.PUBLISHED)
                .recruitStart(now.plusDays(20))
                .recruitEnd(now.plusDays(45))
                .eventStart(now.plusDays(50))
                .eventEnd(now.plusDays(60))
                .isOnline(false)
                .isFree(false)
                .price(10000)
                .thumbnailUrl("https://example.com/thumb/seminar.jpg")
                .applyClicks(30L)
                .viewsCount(0L)
                .likesCount(0L)
                .applyLink("https://example.com/apply/seminar")
                .locationText("판교 테크노밸리")
                .contact("seminar@example.com")
                .recommendedManual(false)
                .ad(false)
                .description("클라우드 최신 동향 공유")
                .build();
        seminarLate.addTargetRole(design);

// D. 부트캠프(모집 종료 — 필터로 제외)
        Event bootcampClosed = Event.builder()
                .title("프론트엔드 부트캠프 (마감)")
                .category(EventCategory.BOOTCAMP_CLUB)
                .status(EventStatus.PUBLISHED)
                .recruitStart(now.minusDays(30))
                .recruitEnd(now.minusDays(1))
                .eventStart(now.plusDays(5))
                .eventEnd(now.plusDays(15))
                .isOnline(false)
                .isFree(true)
                .price(0)
                .thumbnailUrl("https://example.com/thumb/fe.jpg")
                .applyClicks(10L)
                .viewsCount(0L)
                .likesCount(0L)
                .applyLink("https://example.com/apply/fe")
                .locationText("서울 서초")
                .contact("fe@example.com")
                .recommendedManual(false)
                .ad(false)
                .description("프론트엔드 집중 과정")
                .build();
        bootcampClosed.addTargetRole(design);

        eventRepository.saveAll(List.of(bootcampOpen, hackathon, seminarLate, bootcampClosed));

        // 3) 최근 14일 조회수 더미 생성 (EventViewDaily)
        seedViews14(bootcampOpen, 150, 400);   // 150~400 사이 랜덤 합
        seedViews14(hackathon, 80, 250);
        seedViews14(seminarLate, 20, 60);
        seedViews14(bootcampClosed, 10, 30);

        // 4) 좋아요 더미 (EventLike)

        adminRepository.save(
                Admin.builder()
                        .email("user@example.com")
                        .password("string")
                        .role(AdminRole.OWNER)
                        .build()
        );

        Users u1 = usersRepository.save(
                Users.builder()
                        .email("seed1@ex.com")
                        .name("Seed1")
                        .gender("남")
                        .age("15")
                        .jobGroup("개발자")
                        .notificationFlag("Y")
                        .socialId("test")
                        .regDatetime(now)
                        .socialLoginType(SocialLoginType.google)
                        .lastLoginAt(LocalDateTime.now())
                        .status(UserStatus.ACTIVE)
                        .role("일반 사용자")
                        .build()
        );

        Users u2 = usersRepository.save(
                Users.builder()
                        .email("seed2@ex.com")
                        .name("Seed2")
                        .gender("남")
                        .age("16")
                        .jobGroup("디자이너")
                        .regDatetime(now)
                        .notificationFlag("Y")
                        .socialId("test")
                        .socialLoginType(SocialLoginType.kakao)
                        .lastLoginAt(LocalDateTime.now())
                        .status(UserStatus.ACTIVE)
                        .role("일반 사용자")
                        .build()
        );

        eventLikeRepository.saveAll(List.of(
                new EventLike(bootcampOpen, u1),
                new EventLike(bootcampOpen, u2),
                new EventLike(hackathon, u1)
        ));

        //동의어 사전 추가
        makeSynonyms("ko", "카카오 관련 동의어", List.of("카카오", "kakao"));
        makeSynonyms("ko", "우테코 관련 동의어", List.of("우테코", "우아한테크코스", "woowacourse"));
        makeSynonyms("ko", "IT 기업 관련 동의어", List.of("네카라쿠배", "네이버", "카카오", "라인", "쿠팡"));

    }

    private void makeSynonyms(String local, String comment, List<String> terms) {
        SynonymGroup group = synonymGroupRepository.save(
                SynonymGroup.builder()
                        .locale(local)
                        .comment(comment)
                        .build()
        );

        List<SynonymTerm> synonymTermList = terms.stream().map(term -> SynonymTerm.builder()
                        .group(group)
                        .term(term)
                        .build())
                .toList();
        synonymTermRepository.saveAll(synonymTermList);
    }

    private TargetRole getOrCreateRole(String name) {
        return targetRoleRepository.findByName(name)
                .orElseGet(() -> targetRoleRepository.save(TargetRole.builder().name(name).build()));
    }

    private void seedViews14(Event event, int minTotal, int maxTotal) {
        LocalDate today = LocalDate.now();
        int days = 90;

        // 총 합을 대략 min~max 사이로 맞추기 위해 14일 분산
        int targetTotal = ThreadLocalRandom.current().nextInt(minTotal, maxTotal + 1);

        // 기본 분배 + 약간의 랜덤 가중
        int base = targetTotal / days;
        int remain = targetTotal % days;

        for (int i = days - 1; i >= 0; i--) {
            LocalDate d = today.minusDays(i);
            int extra = ThreadLocalRandom.current().nextInt(0, 5); // 소량 가중
            long cnt = base + extra + (remain > 0 ? 1 : 0);
            if (remain > 0) {
                remain--;
            }

            EventViewDaily row = EventViewDaily.builder()
                    .event(event)
                    .cnt(cnt)
                    .build();
            eventViewDailyRepository.save(row);
        }
    }
}
