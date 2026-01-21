package com.example.skillup.domain.event.service;


import static com.example.skillup.domain.event.enums.ActionType.APPLY;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventAction;
import com.example.skillup.domain.event.entity.HashTag;
import com.example.skillup.domain.event.enums.ActionType;
import com.example.skillup.domain.event.enums.ActorType;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventSortType;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.enums.HashTagCategory;
import com.example.skillup.domain.event.repository.EventActionRepository;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.event.repository.HashTagRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
public class EventServiceTimeTest
{

    Event event1;

    @Autowired
    private EventActionRepository eventActionRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private HashTagRepository hashTagRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private EventService eventService;

    Event setUp(Set<HashTag> hashTags,int i) {
        event1 = Event.builder()
                .title("테스트 이벤트"+i)
                .category(EventCategory.BOOTCAMP_CLUB)
                .status(EventStatus.PUBLISHED)
                .isFree(true)
                .isOnline(true)
                .recruitStart(LocalDateTime.now().minusDays(20))
                .recruitEnd(LocalDateTime.now().plusDays(500))
                .eventStart(LocalDateTime.now().minusDays(20))
                .eventEnd(LocalDateTime.now().plusDays(20))
                .hashTags(
                        (hashTags == null || hashTags.isEmpty())
                                ? new HashSet<>()
                                : hashTags
                )
                .build();
        return eventRepository.save(event1);

    }

    @Test
    @Commit
    void  API속도측정() {
        List<HashTag> hashTags = new ArrayList<>();
        for (int i = 0; i < 10; i++)
            hashTags.add(hashTagRepository.save(HashTag.builder()
                    .category(HashTagCategory.EVENT_TYPE).name("#스포츠" + i).build()));

        Random random = new Random();
        List<Event> events = new ArrayList<>();

        for (int i = 1; i <= 1000; i++) {

            int tagCount = random.nextInt(5) + 1;
            Set<HashTag> eventTags = new HashSet<>();

            for (int t = 0; t < tagCount; t++) {
                eventTags.add(hashTags.get(random.nextInt(hashTags.size())));
            }

            events.add(setUp(eventTags, i));
        }

        for (int i = 0; i < 1000; i++) {
            ActionType actionType;
            int tagCount = random.nextInt(2);

            if (tagCount == 0)
                actionType = ActionType.VIEW;
            if (tagCount == 1)
                actionType = APPLY;
            else
                actionType = ActionType.SAVE;

            String actorId = i + "L";
            if (i > 900)
                actorId = "1100L";

            EventAction action
                    = EventAction.builder().event(events.get(i)).actorType(ActorType.USER).actionType(actionType).actorId(actorId).build();
            eventActionRepository.save(action);

        }
        EventRequest.EventSearchCondition condPopularity = EventRequest.EventSearchCondition.builder()
                .category(EventCategory.CONFERENCE_SEMINAR)
                .sort(EventSortType.POPULARITY)
                .page(0)
                .build();


        eventService.getRecommendedEvents(3L , null);
        eventService.getSupplementaryEvents(
                EventCategory.NETWORKING_MENTORING , null);
        eventService.getEventBySearch(condPopularity , null);
        eventService.getEventDetail(3L,null,"3L");
        eventService.getClosingSoonEvents(8 , null);
    }
}
