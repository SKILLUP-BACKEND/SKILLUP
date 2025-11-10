package com.example.skillup.domain.event.service;


import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventBookmark;
import com.example.skillup.domain.event.repository.EventBookmarkRepository;
import com.example.skillup.domain.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EventBookmarkService {

    private EventBookmarkRepository eventBookmarkRepository;

    @Transactional(readOnly = true)
    public boolean isBookmarked(Users user , Event event) {
        return eventBookmarkRepository.findByUserAndEvent(user , event)
                .map(EventBookmark::getIsBookmarked)
                .orElse(false);
    }

}
