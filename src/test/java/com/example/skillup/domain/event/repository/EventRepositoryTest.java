package com.example.skillup.domain.event.repository;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventAction;
import com.example.skillup.domain.event.entity.HashTag;
import com.example.skillup.domain.event.enums.*;
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
import java.util.*;
import java.util.stream.Collectors;

import static com.example.skillup.domain.event.enums.ActionType.APPLY;

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
    private HashTagRepository hashTagRepository;

    @Autowired
    private TestEventActionRepository testEventActionRepository;

    @Autowired
    private TestEventRepository testEventRepository;

    @Autowired
    private EntityManager em;

    Event event1;

    LocalDateTime since = LocalDate.now().minusMonths(3).atStartOfDay();
    LocalDateTime now = LocalDateTime.now();
    private final LocalDate baseDate = LocalDate.of(LocalDate.now().getYear(), 1, 1);

    private LocalDate sequentialDate(int index) {
        return baseDate.plusDays(index % 300);
    }

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
    void findBootcampsOpenOrderByPopularityWithPopularity_Test()
    {
        setUp(null,2);
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
            setUp(null,2);
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
            setUp(null,2);
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

    @Test
    @Commit
    void  해시태그_기반_추천_이벤트_DB_vs_Spring_비교()
    {
        List<HashTag>  hashTags = new ArrayList<>();
        for(int i=0;i<10;i++)
            hashTags.add(hashTagRepository.save(HashTag.builder()
                    .category(HashTagCategory.EVENT_TYPE).name("#스포츠"+i).build()));

        Random random = new Random();
        List<Event> events = new ArrayList<>();

        for (int i = 1; i <= 1000; i++) {

            int tagCount = random.nextInt(5) + 1;
            Set<HashTag> eventTags = new HashSet<>();

            for (int t = 0; t < tagCount; t++) {
                eventTags.add(hashTags.get(random.nextInt(hashTags.size())));
            }

            events.add(setUp(eventTags,i));
        }

        for(int i=0;i<1000;i++)
        {
            ActionType actionType;
            int tagCount = random.nextInt(2);

            if(tagCount == 0)
                actionType = ActionType.VIEW;
            if(tagCount == 1)
                actionType = APPLY;
            else
                actionType = ActionType.SAVE;

            String actorId=i+"L";
            if(i>900)
                actorId="1100L";

            EventAction action
                    = EventAction.builder().event(events.get(i)).actorType(ActorType.USER).actionType(actionType).actorId(actorId).build();
            eventActionRepository.save(action);

        }

        em.flush();
        em.clear();

        // DB에서 처리
        long start1 = System.nanoTime();

        findRecommendedInDB();

        long end1 = System.nanoTime();

        double dbTimeSec = (end1 - start1) / 1_000_000_000.0;

        System.out.println("DB 처리 속도: " + dbTimeSec + "초");

        //스프링에서 처리
        long start2 = System.nanoTime();

        findRecommendedInSpring("1100L");

        long end2 = System.nanoTime();

        double javaTimeSec = (end2 - start2) / 1_000_000_000.0;

        System.out.println("Spring 처리 속도: " + javaTimeSec + "초");


    }
    @Transactional(readOnly = true)
    void findRecommendedInDB() {
        List<Event>top6EventIds=eventRepository.findRecommendedEventForHome(1100L,since);
    }

    @Transactional(readOnly = true)
    void findRecommendedInSpring(String actorId)
    {

        long t1 = System.nanoTime();
        List<TestEventActionRepository.EventActionTagProjection> eaList =
                testEventActionRepository.findEventActionByActorId(actorId);
        long t2 = System.nanoTime();


        long t3 = System.nanoTime();
        List<TestEventRepository.EventTagProjection> events = testEventRepository.findAllNotViewedByUser(actorId);
        long t4 = System.nanoTime();

        long t5 = System.nanoTime();
        Map<Long, Double> hashTagScore = new HashMap<>();

        for (TestEventActionRepository.EventActionTagProjection ea : eaList) {
            double score = switch (ea.getActionType()) {
                case "VIEW" -> 0.3;
                case "SAVE" -> 0.6;
                case "APPLY" -> 0.1;
                default -> 0;
            };
            String tagIdsString = ea.getTagIds();


            for (String tagId : tagIdsString.split(",")) {
                hashTagScore.merge(Long.parseLong(tagId), score, Double::sum);
            }
        }
        long t6 = System.nanoTime();

        long t7 = System.nanoTime();
        Map<Long, Double> top10HashMap =
                hashTagScore.entrySet().stream()
                        .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                        .limit(10)
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                Map.Entry::getValue,
                                (e1, e2) -> e1,
                                HashMap::new
                        ));
        long t8 = System.nanoTime();


        long t9 = System.nanoTime();
        Map<Long, Double> eventScoreMap = new HashMap<>();

        for (TestEventRepository.EventTagProjection event : events) {

            String tagIdsString = event.getTagIds();
            if (tagIdsString == null || tagIdsString.isEmpty()) continue;

            double totalScore = 0.0;

            for (String tagIdStr : tagIdsString.split(",")) {
                Long tagId = Long.parseLong(tagIdStr);
                if (top10HashMap.containsKey(tagId)) {
                    totalScore += top10HashMap.get(tagId);
                }
            }

            eventScoreMap.merge(event.getEventId(), totalScore, Double::sum);
        }
        long t10 = System.nanoTime();


        long t11 = System.nanoTime();
        List<Long> top6EventIds =
                eventScoreMap.entrySet().stream()
                        .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                        .limit(6)
                        .map(Map.Entry::getKey)
                        .toList();
        long t12 = System.nanoTime();


        long t13 = System.nanoTime();
        testEventRepository.findAllByIds(top6EventIds);
        long t14 = System.nanoTime();




        //long t15 = System.nanoTime();
        //testEventActionRepository.findEventActionByActorIdJpa(actorId);
        //long t16 = System.nanoTime();

        //long t17 = System.nanoTime();
        //testEventRepository.findAllNotViewedByUserJpa(actorId);
        //long t18 = System.nanoTime();

        //long t19 = System.nanoTime();
        //eventRepository.findAllById(top6EventIds);
        //long t20 = System.nanoTime();



        print("1 EventAction 조회", t2 - t1);
        //print("1-2) EventAction 조회(최적화 전)", t16 - t15);
        print("2- 유저가 안 본 Event 조회", t4 - t3);
        //print("2-2) 유저가 안 본 Event 조회(최적화 전)", t18 - t17);
        print("3) 유저 해시태그 점수 계산", t6 - t5);
        print("4) Top10 해시태그 선별", t8 - t7);
        print("5) 이벤트별 점수 계산", t10 - t9);
        print("6) Top6 선정", t12 - t11);
        print("7 최종 이벤트 엔티티 조회", t14 - t13);
       // print("7-1) 최종 이벤트 엔티티 조회(최적화 전)", t20 - t19);

    }

    private void print(String label, long nanos) {
        System.out.println(label + " = " + nanos / 1_000_000_000.0);
    }
}
