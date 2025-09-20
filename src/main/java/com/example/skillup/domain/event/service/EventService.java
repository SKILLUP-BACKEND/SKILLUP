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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final TargetRoleRepository targetRoleRepository;

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


}
