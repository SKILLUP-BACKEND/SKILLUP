package com.example.skillup.domain.event.service;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.dto.response.EventResponse.EventApplyResponse;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventAction;
import com.example.skillup.domain.event.entity.EventBanner;
import com.example.skillup.domain.event.entity.EventLike;
import com.example.skillup.domain.event.entity.HashTag;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.enums.ActionType;
import com.example.skillup.domain.event.enums.ActorType;
import com.example.skillup.domain.event.enums.BannerType;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.exception.EventErrorCode;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.exception.HashTagErrorCode;
import com.example.skillup.domain.event.exception.TargetRoleErrorCode;
import com.example.skillup.domain.event.mapper.EventMapper;
import com.example.skillup.domain.event.repository.EventActionRepository;
import com.example.skillup.domain.event.repository.EventBannerRepository;
import com.example.skillup.domain.event.repository.EventLikeRepository;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.event.repository.EventRepositoryImpl;
import com.example.skillup.domain.event.repository.EventViewDailyRepository;
import com.example.skillup.domain.event.repository.HashTagRepository;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.entity.UsersDetails;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.aop.ConvertNotFound;
import com.example.skillup.global.aop.HandleDataAccessException;
import com.example.skillup.global.exception.CommonErrorCode;
import com.example.skillup.global.search.service.EventIndexerService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final TargetRoleRepository targetRoleRepository;
    private final EventLikeRepository eventLikeRepository;
    private final EventBookmarkService eventBookmarkService;
    private final UserRepository userRepository;
    private final EventBannerRepository eventBannerRepository;
    private final EventActionRepository eventActionRepository;
    private final HashTagRepository hashTagRepository;
    private final EventIndexerService eventIndexerService;
    private final EventViewDailyRepository eventViewDailyRepository;
    private final EventViewService eventViewService;

    LocalDateTime since = LocalDate.now().minusMonths(3).atStartOfDay();
    LocalDateTime now = LocalDateTime.now();

    private static final Map<EventCategory, List<EventCategory>> CATEGORY_PRIORITY = Map.of(
            EventCategory.CONFERENCE_SEMINAR, List.of(
                    EventCategory.NETWORKING_MENTORING,
                    EventCategory.COMPETITION_HACKATHON,
                    EventCategory.BOOTCAMP_CLUB
            ),
            EventCategory.NETWORKING_MENTORING, List.of(
                    EventCategory.CONFERENCE_SEMINAR,
                    EventCategory.COMPETITION_HACKATHON,
                    EventCategory.BOOTCAMP_CLUB
            ),
            EventCategory.COMPETITION_HACKATHON, List.of(
                    EventCategory.BOOTCAMP_CLUB,
                    EventCategory.NETWORKING_MENTORING,
                    EventCategory.CONFERENCE_SEMINAR
            ),
            EventCategory.BOOTCAMP_CLUB, List.of(
                    EventCategory.COMPETITION_HACKATHON,
                    EventCategory.NETWORKING_MENTORING,
                    EventCategory.CONFERENCE_SEMINAR
            )
    );


    @Value("${event.popularity.recommend-threshold:70}")
    private double recommendThreshold;

    @ConvertNotFound(
            exception = EventException.class,
            errorCodeEnum = TargetRoleErrorCode.class,
            errorCodeName = "TARGET_ROLE_NOT_FOUND"
    )
    public TargetRole getRole(String name) {
        return targetRoleRepository.findByName(name).orElseThrow();
    }

    @ConvertNotFound(
            exception = EventException.class,
            errorCodeEnum = HashTagErrorCode.class,
            errorCodeName = "HASH_TAG_NOT_FOUND"
    )
    public HashTag getHashTag(String name) {
        return hashTagRepository.findByName(name).orElseThrow();
    }


    @Transactional
    public Event createEvent(EventRequest.CreateEvent request) {
        Event event = eventMapper.toEntity(request);

        request.getTargetRoles().stream()
                .distinct()
                .forEach(roleName -> {
                    TargetRole role = getRole(roleName);
                    event.addTargetRole(role);
                });
        request.getHashTags().stream()
                .distinct()
                .forEach(hashtagName -> {
                    HashTag hashTag = getHashTag(hashtagName);
                    event.addHashTag(hashTag);
                });
        // 중복되는 구조라서 디자인패턴 적용시켜려고 하는데 hashTag, targetRole 겹치는 부분이 여기랑 매퍼 뿐이라서 따로 컴포넌트 만들고 하는게 오히려
        // 더 낭비 같기도 하고 해서 그대로 두기는 했습니다... 좋은 방법 있으시면 추천 부탁드려요

        Event savedEvent = eventRepository.save(event);

        eventIndexerService.index(savedEvent);

        return savedEvent;
    }

    @Transactional
    public EventResponse.CommonEventResponse deleteEvent(Long eventId) {
        Event event = eventRepository.getEvent(eventId);

        if (event.getDeletedAt() != null) {
            throw new EventException(EventErrorCode.EVENT_ALREADY_DELETED, "EventID가 " + eventId + "는");
        }

        event.delete();

        eventIndexerService.delete(event.getId());

        return new EventResponse.CommonEventResponse(event.getId());
    }

    @Transactional
    public EventResponse.CommonEventResponse updateEvent(Long eventId, EventRequest.UpdateEvent request) {
        Event event = eventRepository.getEvent(eventId);

        event.update(request);

        if (request.getTargetRoles() != null && !request.getTargetRoles().isEmpty()) {
            event.getTargetRoles().clear();

            request.getTargetRoles().stream().distinct().forEach(name -> {
                TargetRole role = getRole(name);
                event.addTargetRole(role);
            });
        }

        if (request.getHashTags() != null && !request.getHashTags().isEmpty()) {
            event.getHashTags().clear();
            request.getHashTags().stream().distinct().forEach(name -> {
                HashTag hashTag = getHashTag(name);
                event.addHashTag(hashTag);
            });
        }

        eventIndexerService.index(event);

        return new EventResponse.CommonEventResponse(event.getId());
    }


    @Transactional
    public EventResponse.CommonEventResponse visibilityEvent(Long eventId, boolean isVisible) {
        Event event = eventRepository.getEvent(eventId);

        EventStatus status = (isVisible ? EventStatus.PUBLISHED : EventStatus.HIDDEN);

        event.setStatus(status);

        if (isVisible) {
            eventIndexerService.index(event);
        } else {
            eventIndexerService.delete(event.getId());
        }

        return new EventResponse.CommonEventResponse(event.getId());
    }


    @Transactional
    public EventResponse.EventSelectResponse getEventDetail(Long eventId,
                                                            UsersDetails user,
                                                            String guestId) {
        Event event = eventRepository.getEvent(eventId);

        //admin 아이디의 경우 변형 없음

        boolean isAdmin = false;
        if (user != null) {
            isAdmin = user.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));
        }

        // 일반 사용자는 공개된 게시글 아니면 볼 수 없음
        if (!isAdmin && event.getStatus() != EventStatus.PUBLISHED) {
            throw new EventException(CommonErrorCode.ACCESS_DENIED);
        }

        //일반 사용자라면 북마크 여부 추가해주기
        if (!isAdmin && user != null) {
            boolean isBookmarked = eventBookmarkService.isBookmarked(user.getUser(), event);
        }

        String actorId = user != null ? user.getUser().getId().toString() : guestId;
        ActorType actorType = user != null ? ActorType.USER : ActorType.GUEST;
        ReadEvent(actorId, event, actorType);

        event = eventRepository.getEvent(eventId);

        return eventMapper.toEventDetailInfo(event, false);
    }

    private void ReadEvent(String actorId, Event event, ActorType actorType) {

        //전체 조회수 및 event_view_daily 업데이트
        eventViewService.recordView(event.getId());

        Optional<EventAction> eventAction = eventActionRepository.findByEventAndActorIdAndActionType(event, actorId,
                ActionType.VIEW);

        if (eventAction.isPresent()) {
            eventAction.get().setUpdatedAt();
            return;
        }

        EventAction newEventAction = EventAction.builder()
                .actorId(actorId)
                .actionType(ActionType.VIEW)
                .actorType(actorType)
                .event(event)
                .build();

        eventActionRepository.save(newEventAction);
    }

    @Transactional(readOnly = true)
    public EventResponse.featuredEventResponseList getFeaturedEvents(String tab, int size) {
        String roleName = resolveRoleName(tab);
        String roleFilter = null;

        if (roleName != null) {
            roleFilter = getRole(roleName).getName();
        }

        List<EventRepository.PopularEventProjection> rows = eventRepository.findPopularForHomeWithPopularity(
                roleFilter,
                since,
                LocalDateTime.now(),
                PageRequest.of(0, Math.max(1, size))
        );

        // TODO: 북마크 여부 실제 연동 (현재 false 고정)
        boolean bookmarked = false;

        return eventMapper.toFeaturedEventResponseList(rows.stream()
                .map(r -> {
                    double score = r.getPopularity();
                    Event event = r.getEvent();
                    boolean recommended = event.isRecommendedManual() || score >= recommendThreshold;
                    return eventMapper.toFeaturedEvent(event, bookmarked, recommended, event.isAd(), score);
                })
                .toList(), tab);
    }


    @Transactional(readOnly = true)
    public EventResponse.featuredEventResponseList getClosingSoonEvents(String roleName, int size) {
        LocalDateTime due = now.plusDays(5);

        List<EventRepository.PopularEventProjection> rows = eventRepository.findClosingSoonForHomeWithPopularity(
                roleName, since, now, due, PageRequest.of(0, size)
        );

        List<EventResponse.HomeEventResponse> items = rows.stream()
                .map(r -> {
                    double score = r.getPopularity();
                    Event event = r.getEvent();
                    return eventMapper.toFeaturedEvent(event, false, false, false, score);
                })
                .toList();

        return eventMapper.toFeaturedEventResponseList(items, roleName);
    }

    @Transactional(readOnly = true)
    public EventResponse.CategoryEventResponseList getEventsByCategoryForHome(EventCategory category,
                                                                              int page,
                                                                              int size) {
        Pageable pageable = PageRequest.of(page, size);

        List<EventRepository.PopularEventProjection> rows;
        if (category == EventCategory.BOOTCAMP_CLUB) {
            // 부트캠프/동아리: 모집중만 노출
            rows = eventRepository.findBootcampsOpenOrderByPopularityWithPopularity(since, now, pageable);
        } else {
            // 그 외 카테고리: 마감 30일 이내
            LocalDateTime due = now.plusDays(30);
            rows = eventRepository.findByCategoryWithin30DaysOrderByPopularityWithPopularity(
                    category, since, now, due, pageable);
        }

        List<EventResponse.HomeEventResponse> items = rows.stream()
                .map(r -> {
                    double score = r.getPopularity();
                    Event event = r.getEvent();
                    return eventMapper.toFeaturedEvent(event, false, false, false, score);
                })
                .toList();
        return eventMapper.toCategoryEventResponseList(items, category);
    }

    @Transactional(readOnly = true)
    public EventResponse.EventBannersResponseList getEventBanners() {

        List<EventBanner> mainBanners = eventBannerRepository.findActiveEventBannersByType(BannerType.MAIN_BANNER, now,
                PageRequest.of(0, 5));
        List<EventBanner> subBanner = eventBannerRepository.findActiveEventBannersByType(BannerType.SUB_BANNER, now,
                PageRequest.of(0, 1));

        List<EventResponse.EventBannerResponse> mainEventBanners = eventMapper.toEventBannerResponse(mainBanners);
        List<EventResponse.EventBannerResponse> subEventBanners = eventMapper.toEventBannerResponse(subBanner);

        return eventMapper.toEventBannersResponseList(mainEventBanners, subEventBanners);

    }

    private double calcPopularity(Event event) {
        //현재는 쿼리에서 직접 가져오는걸로 수정
        double views = event.getViewsCount();
        double likes = event.getLikesCount();
        double ctr = (views > 0)
                ? (double) event.getApplyClicks() / views
                : 0.0;
        return views * 0.6 + likes * 0.3 + ctr * 0.1;
    }

    private String resolveRoleName(String tab) {
        if (tab == null || tab.isBlank() || "IT 전체".equals(tab)) {
            return null;
        }
        return switch (tab) {
            case "기획" -> "기획자";
            case "디자인" -> "디자이너";
            case "개발" -> "개발자";
            case "AI" -> "AI 개발자";
            default -> null;
        };
    }

    @Transactional
    public void toggleLike(Event event, Users users) {
        if (eventLikeRepository.existsByEventIdAndUserId(event.getId(), users.getId())) {
            eventLikeRepository.deleteByEventIdAndUserId(event.getId(), users.getId());
            eventRepository.incrementLikes(event.getId(), -1);
        } else {
            eventLikeRepository.save(new EventLike(event, users));
            eventRepository.incrementLikes(event.getId(), 1);
        }
    }

    @Transactional(readOnly = true)
    @HandleDataAccessException
    public EventResponse.SearchEventResponseList getEventBySearch(EventRequest.EventSearchCondition condition) {
        Pageable pageable = PageRequest.of(condition.getPage(), 12);
        List<EventRepositoryImpl.EventWithPopularity> events = eventRepository.findByCategoryWithSearch(condition,
                pageable, since, now);

        boolean targetRolesIsEmpty = condition.getTargetRoles() == null|| condition.getTargetRoles().isEmpty();
        int targetRoleCount = targetRolesIsEmpty ? 0:condition.getTargetRoles().size() ;


        int count = eventRepository.countByCategoryWithSearch(condition.getCategory().name(), condition.getIsOnline()
                , condition.getIsFree(), condition.getStartDate(), condition.getEndDate(), condition.getTargetRoles(),
                now,
                targetRoleCount,
                targetRolesIsEmpty);
        EventResponse.PageInfoResponse pageInfoResponse = EventResponse.PageInfoResponse
                .builder()
                .currentPage(condition.getPage()+1)
                .pageSize(pageable.getPageSize())
                .totalPages((int) Math.ceil((double) count / (pageable.getPageSize())))
                .build();

        return EventResponse.SearchEventResponseList.builder().homeEventResponseList(events.stream()
                .map(r -> {
                    Event event = r.getEvent();
                    double score = r.getPopularity();
                    return eventMapper.toFeaturedEvent(event, false, event.isRecommendedManual(), event.isAd(), score);
                })
                .toList()).total(count).pageInfoResponse(pageInfoResponse).build();
    }

    @Transactional(readOnly = true)
    @HandleDataAccessException
    public List<EventResponse.HomeEventResponse> getSupplementaryEvents(EventCategory category) {

        int MIN_COUNT = 3;
        Pageable pageable = PageRequest.of(0, 4);
        List<EventRepositoryImpl.EventWithPopularity> result = eventRepository.findByCategoryWithSearch
                (EventRequest.EventSearchCondition.builder().category(category).sort("popularity").page(0).build()
                        , pageable, since, now);

        int missing = MIN_COUNT - result.size();

        if (missing > 0) {
            for (EventCategory supplement : CATEGORY_PRIORITY.get(category)) {
                System.out.println(missing);
                List<EventRepositoryImpl.EventWithPopularity> supplementEvents = eventRepository.findByCategoryWithSearch
                        (EventRequest.EventSearchCondition.builder().category(supplement).sort("popularity").page(0)
                                        .build()
                                , pageable, since, now);
                result.addAll(supplementEvents);
                missing -= supplementEvents.size();
                if (missing <= 0) {
                    break;
                }
            }
        }

        return result.stream()
                .map(r -> {
                    Event event = r.getEvent();
                    double score = r.getPopularity();
                    return eventMapper.toFeaturedEvent(event, false, event.isRecommendedManual(), event.isAd(), score);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    @HandleDataAccessException
    public List<EventResponse.HomeEventResponse> getRecommendedEvents(Long actorId) {
        List<Event> events = eventRepository.findRecommendedEventForHome(actorId, since);

        return events.stream()
                .map(event -> {
                    return eventMapper.toFeaturedEvent(event, false, event.isRecommendedManual(), event.isAd(), null);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    @HandleDataAccessException
    public List<EventResponse.HomeEventResponse> getRecentEvents(String actorId) {
        Pageable pageable = PageRequest.of(0, 10);
        List<Event> events = eventActionRepository.findRecentEventsByActorId(actorId, pageable);

        return events.stream()
                .map(event -> {
                    return eventMapper.toFeaturedEvent(event, false, event.isRecommendedManual(), event.isAd(), null);
                })
                .toList();

    }

    @Transactional
    public EventResponse.EventApplyResponse applyEvent(Long eventId, UsersDetails users, String guestId) {
        Event event = eventRepository.getEvent(eventId);

        String actorId = (users != null) ? users.getUser().getId().toString() : guestId;

        Optional<EventAction> eventAction = eventActionRepository.findByEventAndActorIdAndActionType(event, actorId, ActionType.APPLY);
        if(eventAction.isPresent()) {
            return EventResponse.EventApplyResponse.builder()
                    .eventId(eventId)
                    .comment("이미 신청한 이력이 있습니다. 정상적으로 처리 되었습니다.")
                    .build();
        }

        ActorType actorType = users != null ? ActorType.USER : ActorType.GUEST;

        EventAction newEventAction = EventAction.builder()
                .event(event)
                .actorId(actorId)
                .actorType(actorType)
                .actionType(ActionType.APPLY)
                .build();

       eventRepository.incrementApplys(eventId);

        eventActionRepository.save(newEventAction);

        return EventApplyResponse.builder()
                .eventId(eventId)
                .comment("성공적으로 신청되었습니다.")
                .build();
    }
}
