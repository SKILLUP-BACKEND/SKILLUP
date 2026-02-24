package com.example.skillup.domain.event.service;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.request.EventRequest.AdminEventPageRequest;
import com.example.skillup.domain.event.dto.request.EventRequest.UpdateEvent;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.dto.response.EventResponse.AdminDraftEventResponse;
import com.example.skillup.domain.event.dto.response.EventResponse.AdminEventPageResponse;
import com.example.skillup.domain.event.dto.response.EventResponse.EventHashTagResponse;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventAction;
import com.example.skillup.domain.event.entity.HashTag;
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
import com.example.skillup.domain.event.repository.EventRepository.AdminCategoryCountProjection;
import com.example.skillup.domain.event.repository.EventRepository.AdminEventSummaryProjection;
import com.example.skillup.domain.event.repository.EventRepositoryImpl;
import com.example.skillup.domain.event.repository.HashTagRepository;
import com.example.skillup.domain.event.validation.EventPublishValidator;
import com.example.skillup.domain.map.provider.GeocodingProvider.GeoPoint;
import com.example.skillup.domain.map.service.GeocodingService;
import com.example.skillup.domain.user.entity.Guest;
import com.example.skillup.domain.user.entity.UsersDetails;
import com.example.skillup.domain.user.repository.GuestRepository;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.aop.HandleDataAccessException;
import com.example.skillup.global.common.CommonMapper;
import com.example.skillup.global.common.CommonResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
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
    private final GeocodingService geocodingService;
    private final HashTagRepository hashTagRepository;
    private final EventPublishValidator eventPublishValidator;


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

        GeoPoint eventGeoPoint = null;
        if (!request.getIsOnline()) {
            eventGeoPoint = new GeoPoint(request.getLatitude() , request.getLongitude() , request.getLocationText());
        }

        Event event = eventMapper.toEntity(request, thumbnailUrl, eventGeoPoint);

        if (request.getTargetRoles() != null && !request.getTargetRoles().isEmpty()) {
            associationBinder.bindRoles(request.getTargetRoles(), event::addTargetRole);
        }

        if (request.getHashTags() != null && !request.getHashTags().isEmpty()) {
            associationBinder.bindHashTags(request.getHashTags(), event::addHashTag);
        }

        eventPublishValidator.validateForPublish(event);

        Event savedEvent = eventRepository.save(event);

        eventIndexerService.index(savedEvent);

        return savedEvent;
    }

    @Transactional
    public Event createDraftEvent(EventRequest.CreateDraftEvent request, MultipartFile thumbnailImage) {

        String thumbnailUrl = null;
        if (thumbnailImage != null) {
            thumbnailUrl = s3Service.uploadFile(thumbnailImage, "event/thumbnail");
        }

        Event event = eventMapper.toDraftEntity(request, thumbnailUrl);

        if (request.getTargetRoles() != null && !request.getTargetRoles().isEmpty()) {
            associationBinder.bindRoles(request.getTargetRoles(), event::addTargetRole);
        }

        if (request.getHashTags() != null && !request.getHashTags().isEmpty()) {
            associationBinder.bindHashTags(request.getHashTags(), event::addHashTag);
        }

        return eventRepository.save(event);
    }

    @Transactional
    public Event publishDraftEvent(Long eventId , UpdateEvent request, MultipartFile thumbnailImage) {
        Event event = eventRepository.getEvent(eventId);

        if(event.getStatus() != EventStatus.DRAFT) {
            throw new EventException(EventErrorCode.EVENT_ALREADY_PUBLISHED);
        }

        String thumbnailUrl = event.getThumbnailUrl();
        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {
            thumbnailUrl = s3Service.uploadFile(thumbnailImage, "event/thumbnail");
        }

        event.update(request, thumbnailUrl);

        if (request.getTargetRoles() != null && !request.getTargetRoles().isEmpty()) {
            event.getTargetRoles().clear();
            associationBinder.bindRoles(request.getTargetRoles(), event::addTargetRole);
        }

        if (request.getHashTags() != null && !request.getHashTags().isEmpty()) {
            event.getHashTags().clear();
            associationBinder.bindHashTags(request.getHashTags(), event::addHashTag);
        }

        eventPublishValidator.validateForPublish(event);


        if (Boolean.FALSE.equals(event.getIsOnline())) {
            event.updateCoordinates(request.getLatitude(), request.getLongitude());
        }

        event.setStatus(EventStatus.PUBLISHED);
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
        //TODO eventbookmarked 및 eventaction 도 연동해서 지워야할듯
        event.delete();

        eventIndexerService.delete(event.getId());

        return new EventResponse.CommonEventResponse(event.getId());
    }

    @Transactional
    public EventResponse.CommonEventResponse updateEvent(Long eventId, EventRequest.UpdateEvent request,
                                                         MultipartFile thumbnailImage) {
        Event event = eventRepository.getEvent(eventId);

        String oldLocationText = event.getLocationText();
        Boolean oldIsOnline = event.getIsOnline();
        String imageUrl = event.getThumbnailUrl();

        if (thumbnailImage != null && !thumbnailImage.isEmpty()) {

            if (imageUrl != null && !imageUrl.isEmpty()) {
                s3Service.deleteFileFromUrl(imageUrl);
            }

            imageUrl = s3Service.uploadFile(thumbnailImage, "event/thumbnail");
        }

        event.update(request, imageUrl);

        boolean locationChanged =
                request.getLocationText() != null && !request.getLocationText().equals(oldLocationText);

        boolean onlineChanged = request.getIsOnline() != null && !request.getIsOnline().equals(oldIsOnline);

        if (locationChanged || onlineChanged) {
            applyGeocode(event);
        }

        if (request.getTargetRoles() != null && !request.getTargetRoles().isEmpty()) {
            event.getTargetRoles().clear();
            associationBinder.bindRoles(request.getTargetRoles(), event::addTargetRole);
        }

        if (request.getHashTags() != null && !request.getHashTags().isEmpty()) {
            event.getHashTags().clear();
            associationBinder.bindHashTags(request.getHashTags(), event::addHashTag);
        }

        eventPublishValidator.validateForPublish(event);

        eventIndexerService.index(event);

        return new EventResponse.CommonEventResponse(event.getId());
    }

    private void applyGeocode(Event event) {
        if (event.getIsOnline()) {
            event.updateCoordinates(null, null);
            return;
        }

        String address = event.getLocationText();
        if (address == null || address.isBlank()) {
            throw new EventException(EventErrorCode.INVALID_LOCATION_TEXT);
        }

        GeoPoint point = geocodingService.geocode(address);
        log.info("위도 : {} , 경도 : {} , 도로명 주소 : {} ", point.lat(), point.lng(), point.roadAddress());
        event.updateCoordinates(point.lat(), point.lng());
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

        boolean isAdmin = (user != null) && user.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        if (!isAdmin && event.getStatus() != EventStatus.PUBLISHED) {
            throw new EventException(CommonErrorCode.ACCESS_DENIED);
        }

        boolean isBookmarked = false;

        if (isAdmin) {
            return eventMapper.toEventDetailInfo(event, isBookmarked);
        }

        if (user != null) {
            isBookmarked = eventBookmarkService.isBookmarked(user.getUser(), event);
        }

        ActorInfo actor = resolveAndSaveActor(user, guestId);
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

        LocalDateTime since = LocalDate.now().minusMonths(3).atStartOfDay();

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
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime due = now.plusDays(14);

        String roleName = null;
        if (user != null) {
            roleName = notFoundGuardService.getUsersNative(user.getUser().getId()).getRole().getName();
        }

        List<Event> rows = eventRepository.findClosingSoonForHome(
                roleName, now, due, PageRequest.of(0, size)
        );

        List<Long> eventIds = rows.stream().map(Event::getId).toList();

        Set<Long> bookmarkedEventIds = getBookmarkedEventId(user, eventIds);

        List<EventResponse.HomeEventResponse> items = rows.stream()
                .map(event -> {
                    boolean bookmarked = (user != null) && bookmarkedEventIds.contains(event.getId());
                    return eventMapper.toFeaturedEvent(event, bookmarked, false, false, null);
                })
                .toList();

        return eventMapper.toFeaturedEventResponseList(items, roleName);
    }

    @Transactional(readOnly = true)
    public EventResponse.CategoryEventResponseList getEventsByCategoryForHome(EventCategory category,
                                                                              int page,
                                                                              int size, JobGroup tab,
                                                                              UsersDetails user) {

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime twoMonthAgo = now.minusMonths(2);
        Pageable pageable = PageRequest.of(page, size);

        List<EventRepository.PopularEventProjection> rows;
        if (category == EventCategory.BOOTCAMP_CLUB) {
            String roleName = (tab == JobGroup.ALL) ? null : tab.getToKorean();
            // 부트캠프/동아리: 모집중만 노출
            rows = eventRepository.findBootcampsOpenOrderByPopularityWithPopularity(twoMonthAgo, now, roleName,
                    pageable);
        } else {
            // 그 외 카테고리: 마감 30일 이내
            LocalDateTime due = now.plusDays(30);
            rows = eventRepository.findByCategoryWithin30DaysOrderByPopularityWithPopularity(
                    category, twoMonthAgo, now, due, pageable);
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

    @Transactional(readOnly = true)
    @HandleDataAccessException
    public EventResponse.SearchEventResponseList getEventBySearch(EventRequest.EventSearchCondition condition,
                                                                  UsersDetails user) {

        LocalDateTime now = LocalDateTime.now();
        Pageable pageable = PageRequest.of(condition.getPage(), 12);
        List<EventRepositoryImpl.EventWithPopularity> events = findByCategoryWithSearch(condition, pageable);

        List<Long> eventIds = events.stream().map(r -> r.getEvent().getId()).toList();
        Set<Long> bookmarkedEventIds = getBookmarkedEventId(user, eventIds);

        JobGroup targetRole = condition.getTargetRole();
        String targetRoleKr = (targetRole == null || targetRole.equals(JobGroup.ALL)) ? null : targetRole.getToKorean();
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
    public EventHashTagResponse getRecommendedEvents(Long actorId, UsersDetails user) {

        LocalDateTime since = LocalDate.now().minusMonths(3).atStartOfDay();
        List<Event> events = eventRepository.findRecommendedEventForHome(actorId.toString(), actorId, since);

        List<Long> eventIds = events.stream().map(Event::getId).toList();
        Set<Long> bookmarkedEventIds = getBookmarkedEventId(user, eventIds);
        List<HashTag> Top6HashTags = hashTagRepository.findUserTopHashTagEntitiesTop6(actorId.toString(), actorId,
                since);

        //현재는 중복으로 조회를 하는데 해당 중복 구간을 나누기가 어려워서 납뒀습니다.
        //중복으로 하지 않으려면 이벤트 정렬 순서만 없으면 상관없지만 tag_score 점수를 활용해서 이벤트도 점수에따라 순차적으로 내리려면 이렇게 하는 현재로선 방법이 최선인 거 같아요
        //추후에 더 좋은 방법 있으면 리팩토링 해보는것도 좋을 거 같아요

        return eventMapper.toEventHashTagResponse(events, bookmarkedEventIds, Top6HashTags);
    }

    @Transactional(readOnly = true)
    @HandleDataAccessException
    public List<EventResponse.HomeEventResponse> getRecentEvents(String actorId, UsersDetails user) {
        Pageable pageable = PageRequest.of(0, 10);
        List<Event> events = eventActionRepository.findRecentEventsByActorId(actorId, pageable, ActionType.VIEW);

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
        LocalDateTime since = LocalDate.now().minusMonths(3).atStartOfDay();
        LocalDateTime now = LocalDateTime.now();
        return eventRepository.findByCategoryWithSearch
                (condition, pageable, since, now);
    }

    public Set<Long> getBookmarkedEventId(UsersDetails user, List<Long> eventIds) {
        Set<Long> bookmarkedEventIds = new HashSet<>();

        if (user != null && !eventIds.isEmpty()) {
            bookmarkedEventIds.addAll(eventBookmarkRepository.findBookmarkedEventIds(user.getUser().getId(), eventIds));
        }
        return bookmarkedEventIds;
    }


    @Transactional(readOnly = true)
    public AdminEventPageResponse getAdminEventPage(AdminEventPageRequest request) {

        LocalDateTime now = LocalDateTime.now();
        String keyword = request.getKeyword();
        if (keyword != null) {
            keyword = keyword.trim();
            keyword = keyword.isEmpty() ? null : keyword;
        }

        int page = request.getPage();
        Pageable pageable = PageRequest.of(page, 20, toSpringSort(request.getSort()));
        //검색 결과 표시용 event
        Page<Event> result = eventRepository.findAdminEvents(request.getIncludeEnded(), request.getCategory(), keyword,
                now, pageable);

        List<EventResponse.AdminEventRow> rows = eventMapper.toAdminEventRowList(result.getContent(), page, 20,
                result.getTotalElements(), now);
        CommonResponse.PageInfoResponse pageInfoResponse = CommonMapper.toPageInfoResponse(pageable, page,
                (int) result.getTotalElements());

        log.info("total elements: {}", result.getTotalElements());

        //상단바
        AdminEventSummaryProjection countSummary = eventRepository.fetchAdminSummary(request.getIncludeEnded(), now);
        //검색 결과 상단바
        List<AdminCategoryCountProjection> categoryCount = eventRepository.fetchAdminCategoryCounts(
                request.getIncludeEnded(), keyword, now);

        return eventMapper.toAdminEventPageResponse(rows, countSummary, categoryCount, pageInfoResponse);
    }

    private Sort toSpringSort(EventSortType sortType) {
        if (sortType == null) {
            return Sort.by(Sort.Direction.ASC, "eventStart");
        }

        return switch (sortType) {
            case EVENT_START -> Sort.by(Sort.Direction.ASC, "eventStart");
            case VIEWS -> Sort.by(Sort.Direction.DESC, "viewsCount");
            case BOOKMARKS -> Sort.by(Sort.Direction.DESC, "bookmarkedCount");
            case CREATED_AT -> Sort.by(Sort.Direction.DESC, "createdAt");

            default -> throw new EventException(EventErrorCode.INVALID_EVENT_SORT_TYPE, sortType.name() + "은 ");
        };
    }

    public AdminDraftEventResponse getAdminDraftEvents(EventSortType sortType) {

        List<Event> events = switch (sortType) {
            case DEADLINE -> eventRepository.findTop200ByStatusOrderByRecruitEndAsc(EventStatus.DRAFT);
            case CREATED_AT -> eventRepository.findTop200ByStatusOrderByCreatedAtDesc(EventStatus.DRAFT);

            default -> throw new EventException(EventErrorCode.INVALID_EVENT_SORT_TYPE, sortType.name() + "은 ");
        };

        return eventMapper.toAdminDraftEventRowList(events);
    }
}
