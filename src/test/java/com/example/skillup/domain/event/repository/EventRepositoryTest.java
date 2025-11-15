package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventAction;
import com.example.skillup.domain.event.enums.ActionType;
import com.example.skillup.domain.event.enums.ActorType;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.global.common.BaseEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class EventRepositoryTest
{
    @Autowired
    private EventActionRepository eventActionRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EntityManager em;

    Event event1;

    LocalDateTime since = LocalDate.now().minusMonths(3).atStartOfDay();
    LocalDateTime now = LocalDateTime.now();
    private final LocalDate baseDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);

    private LocalDate sequentialDate(int index) {
        return baseDate.plusDays(index % 300);
    }

    @BeforeEach
    void setUp() {
        event1 = Event.builder()
                .title("테스트 이벤트1")
                .category(EventCategory.BOOTCAMP_CLUB)
                .status(EventStatus.PUBLISHED)
                .isFree(true)
                .isOnline(true)
                .recruitStart(LocalDateTime.now().minusDays(20))
                .recruitEnd(LocalDateTime.now().plusDays(500))
                .eventStart(LocalDateTime.now().minusDays(20))
                .eventEnd(LocalDateTime.now().plusDays(20))
                .build();
        eventRepository.save(event1);

    }
    @Test
    void findBootcampsOpenOrderByPopularityWithPopularity_Test()
    {
        Pageable pageable = PageRequest.of(0, 12);
        List<EventRepository.PopularEventProjection> e =
                eventRepository.findBootcampsOpenOrderByPopularityWithPopularity(since,now,pageable);
        System.out.println(e);
    }


    @Test
    void 인덱스가_없는_상황에서_저장() throws NoSuchFieldException, IllegalAccessException {
        long start1 = System.nanoTime();
        for(int i=0;i<9000;i++)
        {
            eventRepository.save(event1);
            EventAction action = EventAction.builder().event(event1)
                    .actorType(ActorType.USER).actionType(ActionType.VIEW).actorId(String.valueOf(i)).build();
            Field field = BaseEntity.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(action, sequentialDate(i/300).atStartOfDay());
            eventActionRepository.save(action);
        }
        long end1 = System.nanoTime();

        double normalTime = (end1 - start1) / 1_000_000_000.0;

        long start2 = System.nanoTime();
        Pageable pageable = PageRequest.of(0, 12);
        for(int i=0;i<1000;i++) {
           //eventActionRepository.findByCreatedAt(sequentialDate(i/300).atStartOfDay());
                 eventRepository.findBootcampsOpenOrderByPopularityWithPopularity(since, now, pageable);
        }
        long end2 = System.nanoTime();

        double normalTime2 = (end2 - start2) / 1_000_000_000.0;
        System.out.println("저장까지 걸린 시간:"+normalTime);
        System.out.println("실행까지 걸린 시간:"+normalTime2);
    }

    @Test
    void 인덱스가_있는_상황에서_저장() throws NoSuchFieldException, IllegalAccessException {
        em.createNativeQuery("CREATE INDEX idx_event_action_eventid_createdat ON event_action (event_id,action_type ,created_at)").executeUpdate();
        long start1 = System.nanoTime();
        for(int i=0;i<9000;i++)
        {
            EventAction action = EventAction.builder().event(event1).actorType(ActorType.USER)
                    .actionType(ActionType.VIEW).actorId(String.valueOf(i)).build();
            Field field = BaseEntity.class.getDeclaredField("createdAt");
            field.setAccessible(true);
            field.set(action, sequentialDate(i/300).atStartOfDay());
            eventActionRepository.save(action);
        }
        long end1 = System.nanoTime();

        double normalTime = (end1 - start1) / 1_000_000_000.0;

        long start2 = System.nanoTime();
        Pageable pageable = PageRequest.of(0, 12);
        for(int i=0;i<1000;i++) {
            //eventActionRepository.findByCreatedAt(sequentialDate(i/300).atStartOfDay());
                 eventRepository.findBootcampsOpenOrderByPopularityWithPopularity(since, now, pageable);
        }
        long end2 = System.nanoTime();

        double normalTime2 = (end2 - start2) / 1_000_000_000.0;
        System.out.println("저장까지 걸린 시간:"+normalTime);
        System.out.println("실행까지 걸린 시간:"+normalTime2);
    }

    @Test
    void 테스트_이벤트_액션_created_at_수동_설정_테스트() throws NoSuchFieldException, IllegalAccessException {

        EventAction action = EventAction.builder().event(event1).actorType(ActorType.USER).actionType(ActionType.VIEW).actorId("3L").build();
        EventAction action2 = EventAction.builder().event(event1).actorType(ActorType.USER).actionType(ActionType.VIEW).actorId("5L").build();

        Field field = BaseEntity.class.getDeclaredField("createdAt");
        field.setAccessible(true);
        field.set(action, sequentialDate(5/300).atStartOfDay());
        eventActionRepository.save(action);
        eventActionRepository.save(action2);
        List<EventAction> ea = eventActionRepository.findAll();
        for(EventAction item : ea)
            System.out.println(item.getCreatedAt());
    }
}
