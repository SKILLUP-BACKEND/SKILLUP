package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventBookmark;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.user.entity.Users;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventBookmarkRepository extends JpaRepository<EventBookmark, Long> {
    Optional<EventBookmark> findByUserAndEvent(Users user, Event event);



    @Query("""
        select eb.event
        from EventBookmark eb
        where eb.user = :user
          and (:category is null or eb.event.category = :category)
        order by eb.createdAt desc
       """)
    List<Event> findEventsByUserWithLatest(@Param("user") Users user,
                                 @Param("category") EventCategory category,
                                           Pageable pageable);


    @Query("""
        select eb.event
        from EventBookmark eb
        where eb.user = :user
          and (:category is null or eb.event.category = :category)
        order by eb.event.recruitEnd asc
       """)
    List<Event> findEventsByUserWithDeadLine(@Param("user") Users user,
                               @Param("category") EventCategory category, Pageable pageable);

}
