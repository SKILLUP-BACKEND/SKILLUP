package com.example.skillup.global.config;

import com.example.skillup.domain.admin.entity.Admin;
import com.example.skillup.domain.admin.enums.AdminRole;
import com.example.skillup.domain.admin.repository.AdminRepository;
import com.example.skillup.domain.article.entity.Article;
import com.example.skillup.domain.article.enums.ArticleStatus;
import com.example.skillup.domain.article.repository.ArticleRepository;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventBanner;
import com.example.skillup.domain.event.entity.EventLike;
import com.example.skillup.domain.event.entity.EventViewDaily;
import com.example.skillup.domain.event.entity.HashTag;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.enums.BannerType;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.enums.HashTagCategory;
import com.example.skillup.domain.event.repository.EventBannerRepository;
import com.example.skillup.domain.event.repository.EventLikeRepository;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.event.repository.EventViewDailyRepository;
import com.example.skillup.domain.event.repository.HashTagRepository;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.enums.UserStatus;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.search.entity.SynonymGroup;
import com.example.skillup.global.search.entity.SynonymTerm;
import com.example.skillup.global.search.repository.SynonymGroupRepository;
import com.example.skillup.global.search.repository.SynonymTermRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private final HashTagRepository hashTagRepository;
    private final EventBannerRepository eventBannerRepository;
    private final ArticleRepository articleRepository;

    @Override
    @Transactional
    public void run(String... args) {

        TargetRole dev = getOrCreateRole("개발자");
        TargetRole design = getOrCreateRole("디자이너");
        TargetRole planner = getOrCreateRole("기획자");
        TargetRole targetRoleTest = getOrCreateRole("string");

        targetRoleRepository.saveAll(List.of(dev, design, planner, targetRoleTest));

        HashTag hashTagTest = getOrCreateHashTag("string");

        hashTagRepository.saveAll(List.of(hashTagTest));

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
                .bookmarkedCount(0L)

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
                .category(EventCategory.CONFERENCE_SEMINAR)
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
                .bookmarkedCount(0L)
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
                .bookmarkedCount(0L)
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
                .bookmarkedCount(0L)
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
                        .email("test@example.com")
                        .name("Seed1")
                        .gender("남")
                        .age("15")
                        .notificationFlag("Y")
                        .socialId("test")
                        .regDatetime(now)
                        .socialLoginType(SocialLoginType.google)
                        .lastLoginAt(LocalDateTime.now())
                        .status(UserStatus.ACTIVE)
                        .role(dev)
                        .build()
        );

        Users u2 = usersRepository.save(
                Users.builder()
                        .email("seed2@ex.com")
                        .name("Seed2")
                        .gender("남")
                        .age("16")
                        .regDatetime(now)
                        .notificationFlag("Y")
                        .socialId("test")
                        .socialLoginType(SocialLoginType.kakao)
                        .lastLoginAt(LocalDateTime.now())
                        .status(UserStatus.ACTIVE)
                        .role(design)
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

        // === 5) 배너 더미 데이터 ===
        LocalDate today = now.toLocalDate();

// ① 현재 노출 중인 메인 배너 1 (부트캠프용)
        EventBanner mainActiveBootcamp = EventBanner.builder()
                .type(BannerType.MAIN_BANNER)
                .bannerImageUrl("https://example.com/banner/backend-main.jpg")
                .mainTitle("🔥 백엔드 부트캠프 지금 모집 중!")
                .subTitle("test")
                .description("test")
                .selected(true)
                // 예: 상세 페이지로 이동하는 링크 (이벤트 id 활용)
                .bannerLink("/events/" + bootcampOpen.getId())
                .displayOrder(1)                     // 가장 위
                .startAt(today.minusDays(5))         // 5일 전 시작
                .endAt(today.plusDays(5))            // 5일 뒤까지 노출
                .build();

// ② 현재 노출 중인 메인 배너 2 (해커톤용)
        EventBanner mainActiveHackathon = EventBanner.builder()
                .type(BannerType.MAIN_BANNER)
                .bannerImageUrl("https://example.com/banner/hackathon-main.jpg")
                .mainTitle("🚀 AI 해커톤 2025 참가자 모집")
                .subTitle("test")
                .description("test")
                .selected(true)
                .bannerLink("/events/" + hackathon.getId())
                .displayOrder(2)                     // 두 번째
                .startAt(today.minusDays(1))         // 어제 시작
                .endAt(today.plusDays(10))           // 10일 뒤까지
                .build();

// ③ 시작 대기 중인 메인 배너 1
        EventBanner mainWaitingSeminar = EventBanner.builder()
                .type(BannerType.MAIN_BANNER)
                .bannerImageUrl("https://example.com/banner/seminar-main.jpg")
                .mainTitle("☁️ 클라우드 세미나 곧 시작 예정")
                .subTitle("test")
                .description("test")
                .selected(true)
                .bannerLink("/events/" + seminarLate.getId())
                .displayOrder(3)
                .startAt(today.plusDays(3))          // 3일 뒤부터 노출
                .endAt(today.plusDays(20))           // 20일 뒤까지
                .build();

// ④ 시작 대기 중인 메인 배너 2
        EventBanner mainWaitingBootcampClosed = EventBanner.builder()
                .type(BannerType.MAIN_BANNER)
                .bannerImageUrl("https://example.com/banner/fe-waiting.jpg")
                .mainTitle("프론트엔드 부트캠프 다음 기수 오픈 예정")
                .subTitle("test")
                .description("test")
                .selected(true)
                .bannerLink("/events/" + bootcampClosed.getId())
                .displayOrder(4)
                .startAt(today.plusDays(7))          // 7일 뒤부터 노출
                .endAt(today.plusDays(30))           // 30일 뒤까지
                .build();

// ⑤ 이미 종료된 과거 메인 배너 1
        EventBanner mainPastOldBootcamp = EventBanner.builder()
                .type(BannerType.MAIN_BANNER)
                .bannerImageUrl("https://example.com/banner/backend-past.jpg")
                .mainTitle("지난 기수 백엔드 부트캠프")
                .subTitle("test")
                .description("test")
                .selected(true)
                .bannerLink("/events/" + bootcampOpen.getId())
                .displayOrder(5)
                .startAt(today.minusDays(30))        // 30일 전 시작
                .endAt(today.minusDays(10))          // 10일 전에 종료 → 과거 배너
                .build();

// ⑥ 이미 종료된 과거 메인 배너 2
        EventBanner mainPastOldHackathon = EventBanner.builder()
                .type(BannerType.MAIN_BANNER)
                .bannerImageUrl("https://example.com/banner/hackathon-past.jpg")
                .mainTitle("지난 해커톤 다시보기")
                .subTitle("test")
                .description("test")
                .selected(true)
                .bannerLink("/events/" + hackathon.getId())
                .displayOrder(6)
                .startAt(today.minusDays(60))
                .endAt(today.minusDays(20))          // 역시 과거
                .build();

        eventBannerRepository.saveAll(List.of(
                mainActiveBootcamp,
                mainActiveHackathon,
                mainWaitingSeminar,
                mainWaitingBootcampClosed,
                mainPastOldBootcamp,
                mainPastOldHackathon
        ));

        // === Article 더미 데이터 ===
        Article a1 = Article.builder()
                .title("Spring Boot 3 성능 최적화 체크리스트")
                .summary("JPA 페이징, 인덱스, 캐시까지 실무에서 바로 쓰는 최적화 포인트를 정리합니다.")
                .thumbnailUrl("https://example.com/thumb/article1.jpg")
                .originalUrl("https://example.com/articles/spring-boot-performance")
                .status(ArticleStatus.PUBLISHED)
                .clickCount(15L)
                .source("SkillUp Blog")
                .originalPublishedDate(LocalDate.now().minusDays(2))
                .build();
        a1.addTargetRole(dev);

        Article a2 = Article.builder()
                .title("디자이너를 위한 UX 리서치 빠른 시작")
                .summary("문제 정의부터 가설, 인터뷰 설계까지 UX 리서치의 핵심 흐름을 소개합니다.")
                .thumbnailUrl("https://example.com/thumb/article2.jpg")
                .originalUrl("https://example.com/articles/ux-research")
                .status(ArticleStatus.PUBLISHED)
                .clickCount(7L)
                .source("Design Weekly")
                .originalPublishedDate(LocalDate.now().minusDays(5))
                .build();
        a2.addTargetRole(design);

        Article a3 = Article.builder()
                .title("서비스 기획자가 알아야 할 A/B 테스트 기본")
                .summary("지표 설계와 실험 설계에서 자주 하는 실수를 중심으로 A/B 테스트를 설명합니다.")
                .thumbnailUrl("https://example.com/thumb/article3.jpg")
                .originalUrl("https://example.com/articles/ab-testing")
                .status(ArticleStatus.PUBLISHED)
                .clickCount(22L)
                .source("Product Note")
                .originalPublishedDate(LocalDate.now().minusDays(1))
                .build();
        a3.addTargetRole(planner);

        Article a4 = Article.builder()
                .title("ElasticSearch n-gram으로 자동완성 구현하기")
                .summary("n-gram 토크나이저와 analyzer 조합으로 검색 자동완성을 구성하는 방법을 정리했습니다.")
                .thumbnailUrl("https://example.com/thumb/article4.jpg")
                .originalUrl("https://example.com/articles/es-ngram-autocomplete")
                .status(ArticleStatus.PUBLISHED)
                .clickCount(40L)
                .source("Search Lab")
                .originalPublishedDate(LocalDate.now().minusDays(3))
                .build();
        a4.addTargetRole(dev);
        a4.addTargetRole(planner);

        Article a5 = Article.builder()
                .title("디자인 시스템 구축 전 체크해야 할 7가지")
                .summary("컴포넌트 기준, 토큰, 문서화 방식 등 디자인 시스템 도입 시 핵심 포인트를 다룹니다.")
                .thumbnailUrl("https://example.com/thumb/article5.jpg")
                .originalUrl("https://example.com/articles/design-system")
                .status(ArticleStatus.PUBLISHED)
                .clickCount(5L)
                .source("UI Archive")
                .originalPublishedDate(LocalDate.now().minusDays(7))
                .build();
        a5.addTargetRole(design);

        Article a6 = Article.builder()
                .title("[DRAFT] 운영 자동화를 위한 모니터링 설계 메모")
                .summary("CloudWatch + Lambda + SNS 기반 이상탐지 흐름을 정리 중입니다.")
                .thumbnailUrl("https://example.com/thumb/article6.jpg")
                .originalUrl("https://example.com/articles/monitoring-draft")
                .status(ArticleStatus.DRAFT)
                .clickCount(0L)
                .source("Internal")
                .originalPublishedDate(LocalDate.now())
                .build();
        a6.addTargetRole(dev);

        articleRepository.saveAll(List.of(a1, a2, a3, a4, a5, a6));

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

    private HashTag getOrCreateHashTag(String name) {
        return hashTagRepository.findByName(name)
                .orElseGet(() -> hashTagRepository.save(
                        HashTag.builder().name(name).category(HashTagCategory.CAREER_GOAL).build()));
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
                    .viewDate(d)
                    .cnt(cnt)
                    .build();
            eventViewDailyRepository.save(row);
        }
    }
}
