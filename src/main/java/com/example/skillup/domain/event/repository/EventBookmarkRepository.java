package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventBookmark;
import com.example.skillup.domain.user.entity.Users;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventBookmarkRepository extends JpaRepository<EventBookmark, Long> {
    Optional<EventBookmark> findByUserAndEvent(Users user, Event event);
}
