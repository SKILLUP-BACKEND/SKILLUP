package com.example.skillup.domain.event.service;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.dto.response.EventResponse.EventApplyResponse;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventAction;
import com.example.skillup.domain.event.entity.EventLike;
import com.example.skillup.domain.event.entity.HashTag;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.enums.ActionType;
import com.example.skillup.domain.event.enums.ActorType;
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
import com.example.skillup.domain.user.entity.Guest;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.entity.UsersDetails;
import com.example.skillup.domain.user.repository.GuestRepository;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.aop.ConvertNotFound;
import com.example.skillup.global.aop.HandleDataAccessException;
import com.example.skillup.global.common.CommonMapper;
import com.example.skillup.global.common.CommonResponse;
import com.example.skillup.global.exception.CommonErrorCode;
import com.example.skillup.global.search.service.EventIndexerService;
import com.example.skillup.global.service.NotFoundGuardService;
import com.example.skillup.global.service.S3Service;
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
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EventLikeRepository eventLikeRepository;
    private final EventBookmarkService eventBookmarkService;
    private final GuestRepository guestRepository;
    private final EventActionRepository eventActionRepository;
    private final EventIndexerService eventIndexerService;
    private final EventViewService eventViewService;
    private final S3Service s3Service;
    private final NotFoundGuardService notFoundGuardService;

    LocalDateTime since = LocalDate.now().minusMonths(3).atStartOfDay();
    LocalDateTime now = LocalDateTime.now();

    private record ActorInfo(String actorId, ActorType actorType) {
    }

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




    @Transactional
    public Event createEvent(EventRequest.CreateEvent request, MultipartFile thumbnailImage) {

        String thumbnailUrl = null;

        if (thumbnailImage != null) {
            thumbnailUrl = s3Service.uploadFile(thumbnailImage, "event/thumbnail");
        }

        Event event = eventMapper.toEntity(request, thumbnailUrl);

        request.getTargetRoles().stream()
                .distinct()
                .forEach(roleName -> {
                    TargetRole role = notFoundGuardService.getRole(roleName);
                    event.addTargetRole(role);
                });
        request.getHashTags().stream()
                .distinct()
                .forEach(hashtagName -> {
                    HashTag hashTag = notFoundGuardService.getHashTag(hashtagName);
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

        //현재 소프트 삭제를 하고 있어서 사진은 S3 에 납두는걸로 로직을 작성했습니다.
        //s3Service.deleteFileFromUrl(event.getThumbnailUrl());

        if (event.getDeletedAt() != null) {
            throw new EventException(EventErrorCode.EVENT_ALREADY_DELETED, "EventID가 " + eventId + "는");
        }

        event.delete();

        eventIndexerService.delete(event.getId());

        return new EventResponse.CommonEventResponse(event.getId());
    }

    @Transactional
    public EventResponse.CommonEventResponse updateEvent(Long eventId, EventRequest.UpdateEvent request,
                                                         MultipartFile thumbnailImage) {
        Event event = eventRepository.getEvent(eventId);

        String imageUrl = event.getThumbnailUrl();

        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {

            if (imageUrl != null && !imageUrl.isEmpty()) {
                s3Service.deleteFileFromUrl(imageUrl);
            }

            imageUrl = s3Service.uploadFile(thumbnailImage, "event/thumbnail");
        }

        event.update(request, imageUrl);

        if (request.getTargetRoles() != null && !request.getTargetRoles().isEmpty()) {
            event.getTargetRoles().clear();

            request.getTargetRoles().stream().distinct().forEach(name -> {
                TargetRole role = notFoundGuardService.getRole(name);
                event.addTargetRole(role);
            });
        }

        if (request.getHashTags() != null && !request.getHashTags().isEmpty()) {
            event.getHashTags().clear();
            request.getHashTags().stream().distinct().forEach(name -> {
                HashTag hashTag = notFoundGuardService.getHashTag(name);
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

        ActorInfo actor = resolveAndSaveActor(user, guestId);

        boolean isAdmin = (user != null) && user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        if (!isAdmin && event.getStatus() != EventStatus.PUBLISHED) {
            throw new EventException(CommonErrorCode.ACCESS_DENIED);
        }

        boolean isBookmarked = false;
        if (user != null && !isAdmin) {
            isBookmarked = eventBookmarkService.isBookmarked(user.getUser(), event);
        }

        readEvent(actor.actorId, event, actor.actorType);

        event = eventRepository.getEvent(eventId);

        return eventMapper.toEventDetailInfo(event, isBookmarked);
    }

    private void saveOrUpdateGuest(String guestId) {
        Guest guest = guestRepository.findById(guestId)
                .orElseGet(() -> Guest.builder().guestId(guestId).expiredAt(LocalDateTime.now().plusDays(30)).build());

        guest.extendExpiration(30);
        guestRepository.save(guest);
    }

    private void readEvent(String actorId, Event event, ActorType actorType) {

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
            roleFilter = notFoundGuardService.getRole(roleName).getName();
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

        boolean targetRolesIsEmpty = condition.getTargetRoles() == null || condition.getTargetRoles().isEmpty();
        int targetRoleCount = targetRolesIsEmpty ? 0 : condition.getTargetRoles().size();

        int count = eventRepository.countByCategoryWithSearch(condition.getCategory().name(), condition.getIsOnline()
                , condition.getIsFree(), condition.getStartDate(), condition.getEndDate(), condition.getTargetRoles(),
                now,
                targetRoleCount,
                targetRolesIsEmpty);
      
        return eventMapper.toCategoryPageEventResponseListWithPageable(events,pageable,condition.getPage(),count);
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

        return eventMapper.toCategoryPageEventResponseList(result);
    }

    @Transactional(readOnly = true)
    @HandleDataAccessException
    public List<EventResponse.HomeEventResponse> getRecommendedEvents(Long actorId) {
        List<Event> events = eventRepository.findRecommendedEventForHome(actorId, since);

        return eventMapper.toHomeEventResponsList(events);
    }

    @Transactional(readOnly = true)
    @HandleDataAccessException
    public List<EventResponse.HomeEventResponse> getRecentEvents(String actorId) {
        Pageable pageable = PageRequest.of(0, 10);
        List<Event> events = eventActionRepository.findRecentEventsByActorId(actorId, pageable);

        return eventMapper.toHomeEventResponsList(events);

    }

    @Transactional
    public EventResponse.EventApplyResponse applyEvent(Long eventId, UsersDetails users, String guestId) {
        Event event = eventRepository.getEvent(eventId);

        ActorInfo actor = resolveAndSaveActor(users, guestId);

        Optional<EventAction> eventAction = eventActionRepository.findByEventAndActorIdAndActionType(event,
                actor.actorId,
                ActionType.APPLY);

        if (eventAction.isPresent()) {
            return EventResponse.EventApplyResponse.builder()
                    .eventId(eventId)
                    .comment("이미 신청한 이력이 있습니다. 정상적으로 처리 되었습니다.")
                    .build();
        }
        EventAction newEventAction = EventAction.builder()
                .event(event)
                .actorId(actor.actorId)
                .actorType(actor.actorType)
                .actionType(ActionType.APPLY)
                .build();

        eventRepository.incrementApplys(eventId);

        eventActionRepository.save(newEventAction);

        return eventMapper.toEventApplyResponse(eventId);
    }

    private ActorInfo resolveAndSaveActor(UsersDetails user, String guestId) {
        if (user != null) {
            return new ActorInfo(user.getUser().getId().toString(), ActorType.USER);
        }
        saveOrUpdateGuest(guestId);
        return new ActorInfo(guestId, ActorType.GUEST);
    }
}
