package com.example.skillup.domain.event.service;


import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventBookmark;
import com.example.skillup.domain.event.exception.EventErrorCode;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.repository.EventBookmarkRepository;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.entity.UsersDetails;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventBookmarkService {

    private final EventBookmarkRepository eventBookmarkRepository;
    private final EventRepository eventRepository;

    @Transactional
    public EventBookmark updateBookmarked(UsersDetails user, Long eventId) {

        if (user == null) {
            throw new EventException(EventErrorCode.EVENT_INCORRECT_USER, "북마크 기능은 로그인한 일반 사용자만 사용 가능합니다.");
        }

        Event event = eventRepository.getEvent(eventId);

        Optional<EventBookmark> eventBookmarkOptional = eventBookmarkRepository.findByUserAndEvent(user.getUser(),
                event);
        if (eventBookmarkOptional.isPresent()) {
            eventBookmarkOptional.get().changeBookmarked();
            return eventBookmarkOptional.get();
        }

        EventBookmark eventBookmark = EventBookmark.builder()
                .event(event)
                .user(user.getUser())
                .isBookmarked(true)
                .build();
        eventBookmarkRepository.save(eventBookmark);
        return eventBookmark;
    }


    @Transactional(readOnly = true)
    public boolean isBookmarked(Users user, Event event) {
        return eventBookmarkRepository.findByUserAndEvent(user, event)
                .map(EventBookmark::getIsBookmarked)
                .orElse(false);
    }

}
