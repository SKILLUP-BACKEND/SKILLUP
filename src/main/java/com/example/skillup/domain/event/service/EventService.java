package com.example.skillup.domain.event.service;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.exception.EventErrorCode;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.exception.TargetRoleErrorCode;
import com.example.skillup.domain.event.mapper.EventMapper;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
}
