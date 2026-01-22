package com.example.skillup.domain.event.service;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventAction;
import com.example.skillup.domain.event.entity.EventLike;
import com.example.skillup.domain.event.enums.ActionType;
import com.example.skillup.domain.event.enums.ActorType;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventSortType;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.exception.EventErrorCode;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.mapper.EventMapper;
import com.example.skillup.domain.event.repository.EventActionRepository;
import com.example.skillup.domain.event.repository.EventBookmarkRepository;
import com.example.skillup.domain.event.repository.EventLikeRepository;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.event.repository.EventRepositoryImpl;
import com.example.skillup.domain.user.entity.Guest;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.entity.UsersDetails;
import com.example.skillup.domain.user.repository.GuestRepository;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.aop.HandleDataAccessException;
import com.example.skillup.global.enums.JobGroup;
import com.example.skillup.global.exception.CommonErrorCode;
import com.example.skillup.global.search.service.EventIndexerService;
import com.example.skillup.global.service.AssociationBinder;
import com.example.skillup.global.service.NotFoundGuardService;
import com.example.skillup.global.service.S3Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private final AssociationBinder associationBinder;
    private final NotFoundGuardService notFoundGuardService;
    private final EventBookmarkRepository eventBookmarkRepository;
    private final UserRepository userRepository;

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

        if (request.getTargetRoles() != null && !request.getTargetRoles().isEmpty()) {
            associationBinder.bindRoles(request.getTargetRoles(), event::addTargetRole);
        }

        if (request.getHashTags() != null && !request.getHashTags().isEmpty()) {
            associationBinder.bindHashTags(request.getHashTags(), event::addHashTag);
        }

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
            associationBinder.bindRoles(request.getTargetRoles(), event::addTargetRole);
        }

        if (request.getHashTags() != null && !request.getHashTags().isEmpty()) {
            event.getHashTags().clear();
            associationBinder.bindHashTags(request.getHashTags(), event::addHashTag);
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
    public EventResponse.featuredEventResponseList getFeaturedEvents(JobGroup tab, int size, UsersDetails user) {
        String roleName = (tab == JobGroup.ALL) ? null : tab.getToKorean();
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

        List<Long> EventIds = rows.stream().map(r -> r.getEvent().getId()).toList();

        Set<Long> bookmarkedEventIds = getBookmarkedEventId(user, EventIds);

        return eventMapper.toFeaturedEventResponseList(rows.stream()
                .map(r -> {
                    double score = r.getPopularity();
                    Event event = r.getEvent();
                    boolean recommended = event.isRecommendedManual() || score >= recommendThreshold;
                    boolean bookmarked = (user != null) && bookmarkedEventIds.contains(event.getId());
                    return eventMapper.toFeaturedEvent(event, bookmarked, recommended, event.isAd(), score);
                })
                .toList(), tab.getToKorean());
    }


    @Transactional(readOnly = true)
    public EventResponse.featuredEventResponseList getClosingSoonEvents(int size, UsersDetails user) {
        LocalDateTime due = now.plusDays(14);

        String roleName = null;
        if (user != null) {
            roleName = notFoundGuardService.getUsersNative(user.getUser().getId()).getRole().getName();
        }

        List<EventRepository.PopularEventProjection> rows = eventRepository.findClosingSoonForHomeWithPopularity(
                roleName, since, now, due, PageRequest.of(0, size)
        );

        List<Long> eventIds = rows.stream().map(r -> r.getEvent().getId()).toList();

        Set<Long> bookmarkedEventIds = getBookmarkedEventId(user, eventIds);

        List<EventResponse.HomeEventResponse> items = rows.stream()
                .map(r -> {
                    double score = r.getPopularity();
                    Event event = r.getEvent();
                    boolean bookmarked = (user != null) && bookmarkedEventIds.contains(event.getId());
                    return eventMapper.toFeaturedEvent(event, bookmarked, false, false, score);
                })
                .toList();

        return eventMapper.toFeaturedEventResponseList(items, roleName);
    }

    @Transactional(readOnly = true)
    public EventResponse.CategoryEventResponseList getEventsByCategoryForHome(EventCategory category,
                                                                              int page,
                                                                              int size, JobGroup tab,
                                                                              UsersDetails user) {
        Pageable pageable = PageRequest.of(page, size);

        List<EventRepository.PopularEventProjection> rows;
        if (category == EventCategory.BOOTCAMP_CLUB) {
            String roleName = (tab == JobGroup.ALL) ? null : tab.getToKorean();
            // 부트캠프/동아리: 모집중만 노출
            rows = eventRepository.findBootcampsOpenOrderByPopularityWithPopularity(since, now, roleName, pageable);
        } else {
            // 그 외 카테고리: 마감 30일 이내
            LocalDateTime due = now.plusDays(30);
            rows = eventRepository.findByCategoryWithin30DaysOrderByPopularityWithPopularity(
                    category, since, now, due, pageable);
        }

        List<Long> eventIds = rows.stream().map(r -> r.getEvent().getId()).toList();
        Set<Long> bookmarkedEventIds = getBookmarkedEventId(user, eventIds);

        List<EventResponse.HomeEventResponse> items = rows.stream()
                .map(r -> {
                    double score = r.getPopularity();
                    Event event = r.getEvent();
                    boolean bookmarked = (user != null) && bookmarkedEventIds.contains(event.getId());
                    return eventMapper.toFeaturedEvent(event, bookmarked, false, false, score);
                })
                .toList();
        return eventMapper.toCategoryEventResponseList(items, category);
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
    public EventResponse.SearchEventResponseList getEventBySearch(EventRequest.EventSearchCondition condition,
                                                                  UsersDetails user) {
        Pageable pageable = PageRequest.of(condition.getPage(), 12);
        List<EventRepositoryImpl.EventWithPopularity> events = findByCategoryWithSearch(condition, pageable);

        List<Long> eventIds = events.stream().map(r -> r.getEvent().getId()).toList();
        Set<Long> bookmarkedEventIds = getBookmarkedEventId(user, eventIds);

        JobGroup targetRole = condition.getTargetRole();
        String targetRoleKr = (targetRole == null) ? null : targetRole.getToKorean();
        boolean targetRolesIsEmpty = (targetRoleKr == null || targetRoleKr.isEmpty());

        int count = eventRepository.countByCategoryWithSearch(condition.getCategory().name(), condition.getIsOnline()
                , condition.getIsFree(), condition.getStartDate(), condition.getEndDate(), targetRoleKr,
                now,
                targetRolesIsEmpty);

        return eventMapper.toCategoryPageEventResponseListWithPageable(events, pageable, condition.getPage(), count,
                bookmarkedEventIds);
    }

    @Transactional(readOnly = true)
    @HandleDataAccessException
    public List<EventResponse.HomeEventResponse> getSupplementaryEvents(EventCategory category, UsersDetails user) {

        int MIN_COUNT = 3;
        Pageable pageable = PageRequest.of(0, 4);
        List<EventRepositoryImpl.EventWithPopularity> result = findByCategoryWithSearch
                (EventRequest.EventSearchCondition.of(category, null, null, null, null, EventSortType.POPULARITY, null,
                                0)
                        , pageable);

        int missing = MIN_COUNT - result.size();

        if (missing > 0) {
            for (EventCategory supplement : CATEGORY_PRIORITY.get(category)) {
                List<EventRepositoryImpl.EventWithPopularity> supplementEvents = findByCategoryWithSearch
                        (EventRequest.EventSearchCondition.of(supplement, null, null, null, null,
                                        EventSortType.POPULARITY, null, 0)
                                , pageable);
                result.addAll(supplementEvents);
                missing -= supplementEvents.size();
                if (missing <= 0) {
                    break;
                }
            }
        }

        List<Long> eventIds = result.stream().map(r -> r.getEvent().getId()).toList();
        Set<Long> bookmarkedEventIds = getBookmarkedEventId(user, eventIds);

        return eventMapper.toCategoryPageEventResponseList(result, bookmarkedEventIds);
    }


    @Transactional(readOnly = true)
    @HandleDataAccessException
    public List<EventResponse.HomeEventResponse> getRecommendedEvents(Long actorId, UsersDetails user) {
        List<Event> events = eventRepository.findRecommendedEventForHome(actorId, since);

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Set<Long> bookmarkedEventIds = getBookmarkedEventId(user, eventIds);

        return eventMapper.toHomeEventResponsList(events, bookmarkedEventIds);
    }

    @Transactional(readOnly = true)
    @HandleDataAccessException
    public List<EventResponse.HomeEventResponse> getRecentEvents(String actorId, UsersDetails user) {
        Pageable pageable = PageRequest.of(0, 10);
        List<Event> events = eventActionRepository.findRecentEventsByActorId(actorId, pageable);

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Set<Long> bookmarkedEventIds = getBookmarkedEventId(user, eventIds);

        return eventMapper.toHomeEventResponsList(events, bookmarkedEventIds);

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

    private List<EventRepositoryImpl.EventWithPopularity> findByCategoryWithSearch
            (EventRequest.EventSearchCondition condition, Pageable pageable) {
        return eventRepository.findByCategoryWithSearch
                (condition, pageable, since, now);
    }

    private Set<Long> getBookmarkedEventId(UsersDetails user, List<Long> eventIds) {
        Set<Long> bookmarkedEventIds = new HashSet<>();

        if (user != null && !eventIds.isEmpty()) {
            bookmarkedEventIds.addAll(eventBookmarkRepository.findBookmarkedEventIds(user.getUser().getId(), eventIds));
        }
        return bookmarkedEventIds;
    }
}
