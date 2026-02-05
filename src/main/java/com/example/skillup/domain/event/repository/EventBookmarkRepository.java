package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventBookmark;
import com.example.skillup.domain.user.entity.Users;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EventBookmarkRepository extends JpaRepository<EventBookmark, Long> {
    Optional<EventBookmark> findByUserAndEvent(Users user, Event event);



    @Query("""
        select eb.event
        from EventBookmark eb
        where eb.user = :user and eb.isBookmarked = true
        order by eb.createdAt desc
       """)
    Page<Event> findEventsByUserWithLatest(@Param("user") Users user,
                                           Pageable pageable);


    @Query("""
        select eb.event
        from EventBookmark eb
        where eb.user = :user and eb.isBookmarked = true
        order by eb.event.recruitEnd asc
       """)
    Page<Event> findEventsByUserWithDeadLine(@Param("user") Users user,
                              Pageable pageable);


    @Query("""
        select eb.event.id
        from EventBookmark eb
        where eb.user.id = :userId
          and eb.event.id in :eventIds
          and eb.isBookmarked = true
    """)
    List<Long> findBookmarkedEventIds(@Param("userId") Long userId,
                                      @Param("eventIds") List<Long> eventIds);
}
