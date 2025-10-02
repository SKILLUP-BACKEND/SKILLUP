package com.example.skillup.domain.event.service;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.exception.EventErrorCode;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.exception.TargetRoleErrorCode;
import com.example.skillup.domain.event.mapper.EventMapper;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final TargetRoleRepository targetRoleRepository;

    @Value("${event.popularity.recommend-threshold:70}")
    private double recommendThreshold;

    @Transactional
    public Event createEvent(EventRequest.CreateEvent request) {
        Event event = eventMapper.toEntity(request);

        request.getTargetRoles().stream()
                .distinct()
                .forEach(roleName -> {
                    TargetRole role = targetRoleRepository.findByName(roleName)
                            .orElseThrow(() -> new EventException(TargetRoleErrorCode.TARGET_ROLE_NOT_FOUND , roleName+"에"));
                    event.addTargetRole(role);
                });

        return eventRepository.save(event);
    }

    @Transactional
    public EventResponse.CommonEventResponse deleteEvent(Long eventId) {
        Event event = eventRepository.getEvent(eventId);

        if (event.getDeletedAt() != null) {
            throw new EventException(EventErrorCode.EVENT_ALREADY_DELETED, "EventID가 " + eventId + "는");
        }
        event.delete();

        return new EventResponse.CommonEventResponse(event.getId());
    }

    @Transactional
    public EventResponse.CommonEventResponse updateEvent(Long eventId, EventRequest.UpdateEvent request) {
        Event event = eventRepository.getEvent(eventId);

        event.update(request);

        if (request.getTargetRoles() != null && !request.getTargetRoles().isEmpty()) {
            event.getTargetRoles().forEach(role -> role.getEvents().remove(event));
            event.getTargetRoles().clear();

            request.getTargetRoles().stream().distinct().forEach(name -> {
                TargetRole role = targetRoleRepository.findByName(name)
                        .orElseThrow(() -> new EventException(TargetRoleErrorCode.TARGET_ROLE_NOT_FOUND, name + "에"));
                event.addTargetRole(role);
            });
        }

        return new EventResponse.CommonEventResponse(event.getId());
    }


    @Transactional
    public EventResponse.CommonEventResponse hideEvent(Long eventId) {
        Event event = eventRepository.getEvent(eventId);

        if (event.getStatus() == EventStatus.HIDDEN) {
            throw new EventException(EventErrorCode.EVENT_ALREADY_HIDDEN, "EventID가 " + eventId + "는");
        }

        event.setStatus(EventStatus.HIDDEN);

        return new EventResponse.CommonEventResponse(event.getId());
    }

    @Transactional
    public EventResponse.CommonEventResponse publishEvent(Long eventId) {
        Event event = eventRepository.getEvent(eventId);
        if (event.getStatus() == EventStatus.PUBLISHED) {
            throw new EventException(EventErrorCode.EVENT_ALREADY_PUBLISHED ,"EventID가 " + eventId + "는");
        }

        event.setStatus(EventStatus.PUBLISHED);
        return new EventResponse.CommonEventResponse(event.getId());
    }

    @Transactional(readOnly = true)
    public EventResponse.EventSelectResponse getEventDetail(Long eventId, Collection<? extends GrantedAuthority> authorities) {
        Event event = eventRepository.getEvent(eventId);

        boolean isAdmin = authorities.stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER"));

        // 일반 사용자는 공개된 게시글 아니면 볼 수 없음
        if (!isAdmin && event.getStatus() != EventStatus.PUBLISHED) {
            throw new EventException(CommonErrorCode.ACCESS_DENIED);
        }

        return eventMapper.toEventDetailInfo(event);
    }

    @Transactional(readOnly = true)
    public EventResponse.featuredEventResponseList getFeaturedEvents(String tab, int size) {
        String roleName = resolveRoleName(tab);

        String roleFilter = null;

        if (roleName != null) {
            roleFilter = targetRoleRepository.findByName(roleName)
                    .map(TargetRole::getName)
                    .orElseThrow(()-> new EventException(TargetRoleErrorCode.TARGET_ROLE_NOT_FOUND, roleName));
        }

        List<Event> events = eventRepository.findPopularForHome(
                roleFilter,
                LocalDateTime.now(),
                PageRequest.of(0, Math.max(1, size))
        );

        // TODO: 북마크 여부 실제 연동 (현재 false 고정)
        boolean bookmarked = false;

        return  eventMapper.toFeaturedEventResponsList(events.stream()
                .map(e -> {
                    double score = calcPopularity(e);
                    boolean recommended = e.isRecommendedManual() || score >= recommendThreshold;
                    return eventMapper.toFeaturedEvent(e, bookmarked, recommended, e.isAd(),score);
                })
                .toList() , tab );
    }

    @Transactional(readOnly = true)
    public EventResponse.featuredEventResponseList getClosingSoonEvents(String roleName, int size) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime due = now.plusDays(5);

        List<Event> events = eventRepository.findClosingSoonForHome(
                roleName, now, due, PageRequest.of(0, size)
        );

        List<EventResponse.FeaturedEventResponse> items = events.stream()
                .map(e -> {
                    double score = calcPopularity(e);
                    return eventMapper.toFeaturedEvent(e, false, false, false, score);
                })
                .toList();

        return eventMapper.toFeaturedEventResponsList(items, roleName);
    }

    private double calcPopularity(Event event) {
        double views = event.getViewsCount();
        double likes = event.getLikesCount();
        double ctr   = (event.getApplyImpressions() > 0)
                ? (double) event.getApplyClicks() / event.getApplyImpressions()
                : 0.0;
        return views * 0.6 + likes * 0.3 + ctr * 0.1;
    }

    private String resolveRoleName(String tab) {
        if (tab == null || tab.isBlank() || "IT 전체".equals(tab)) return null;
        return switch (tab) {
            case "기획" -> "기획자";
            case "디자인" -> "디자이너";
            case "개발" -> "개발자";
            case "AI" -> "AI 개발자";
            default -> null;
        };
    }
}

