package com.example.skillup.domain.event.service;


import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.*;
import com.example.skillup.domain.event.enums.*;
import com.example.skillup.domain.event.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import javax.sound.midi.SysexMessage;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EventServiceTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventService eventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TargetRoleRepository targetRoleRepository;

    @Autowired
    private EventViewDailyRepository eventViewDailyRepository;

    @Autowired
    private EventActionRepository  eventActionRepository;
    @Autowired
    private HashTagRepository hashTagRepository;

    private HashTag hashTag;
    private HashTag hashTag2;
    private HashTag hashTag3;
    private HashTag hashTag4;
    private HashTag hashTag5;
    private HashTag hashTag6;
    private HashTag hashTag7;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        targetRoleRepository.deleteAll();
        targetRoleRepository.save(TargetRole.builder().name("PLANNER").build());
        targetRoleRepository.save(TargetRole.builder().name("DESIGNER").build());
        targetRoleRepository.save(TargetRole.builder().name("AI_DEVELOPER").build());
        hashTag=hashTagRepository.save(HashTag.builder().category(HashTagCategory.EVENT_TYPE).name("#스포츠").build());
        hashTag2=hashTagRepository.save(HashTag.builder().category(HashTagCategory.EVENT_TYPE).name("#러닝").build());
        hashTag3=hashTagRepository.save(HashTag.builder().category(HashTagCategory.EVENT_TYPE).name("#서울").build());
        hashTag4=hashTagRepository.save(HashTag.builder().category(HashTagCategory.EVENT_TYPE).name("#IT").build());
        hashTag5=hashTagRepository.save(HashTag.builder().category(HashTagCategory.EVENT_TYPE).name("#AI").build());
        hashTag6=hashTagRepository.save(HashTag.builder().category(HashTagCategory.EVENT_TYPE).name("#워크숍").build());
        hashTag7=hashTagRepository.save(HashTag.builder().category(HashTagCategory.EVENT_TYPE).name("#PLANNER").build());

    }

    private Event createEvent(String title) {
        return createEvent(title, EventCategory.BOOTCAMP_CLUB); // 기본값
    }

    private Event createEvent(String title, EventCategory category) {
        Set<TargetRole> roles = targetRoleRepository.findAll().stream()
                .filter(r -> r.getName().equals("DESIGNER"))
                .collect(Collectors.toSet());
        Set<HashTag> tags = new HashSet<>();
        tags.add(hashTagRepository.findByName("#PLANNER").orElseThrow());

        return Event.builder()
                .title(title)
                .status(EventStatus.PUBLISHED)
                .eventStart(LocalDateTime.of(2025, 9, 12, 10, 0))
                .eventEnd(LocalDateTime.of(2025, 11, 12, 12, 0))
                .thumbnailUrl("http://example.com/thumb.png")
                .category(category) // 전달받은 값 사용
                .recruitEnd(LocalDateTime.of(2025, 9, 12, 12, 0))
                .recruitStart(LocalDateTime.of(2025, 9, 12, 10, 0))
                .isFree(true)
                .price(null)
                .isOnline(true)
                .locationLink("http://example.com")
                .locationText("test")
                .applyLink("http://apply.example.com")
                .contact("010-1234-5678")
                .description("test")
                .hashTags(new HashSet<>(tags))
                .targetRoles(new HashSet<>(roles))
                .build();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"OWNER"})
    void createEvent_Success_Test() throws Exception {

        EventRequest.CreateEvent request = new EventRequest.CreateEvent(
                "행사 생성 테스트",
                "http://example.com/thumb.png",
                EventCategory.BOOTCAMP_CLUB,
                LocalDateTime.of(2025, 9, 12, 10, 0),
                LocalDateTime.of(2025, 9, 12, 12, 0),
                LocalDateTime.of(2025, 9, 1, 0, 0),
                LocalDateTime.of(2025, 9, 10, 23, 59),
                true,
                null,
                List.of("DESIGNER"),
                false,
                true,
                "서울 올림픽공원",
                "http://maps.example.com",
                "http://apply.example.com",
                "010-1234-5678",
                "이벤트 설명입니다",
                List.of("#스포츠","#러닝")
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventId").isNumber())
                .andExpect(jsonPath("$.message").value("행사가 등록되었습니다."))
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        Event savedEvent = eventRepository.findAll().get(0);
        assertThat(savedEvent.getTitle()).isEqualTo("행사 생성 테스트");
        assertThat(savedEvent.getStatus()).isEqualTo(EventStatus.PUBLISHED);

    }

    @Test
    @WithMockUser(username = "admin", roles = {"OWNER"})
    void createEvent_Draft_Success_Test() throws Exception {
        EventRequest.CreateEvent request = new EventRequest.CreateEvent(
                "임시 저장 테스트",
                "http://example.com/thumb.png",
                EventCategory.BOOTCAMP_CLUB,
                LocalDateTime.of(2025, 9, 12, 10, 0),
                LocalDateTime.of(2025, 9, 12, 12, 0),
                LocalDateTime.of(2025, 9, 1, 0, 0),
                LocalDateTime.of(2025, 9, 10, 23, 59),
                true,
                null,
                List.of("DESIGNER"),
                true,   // 임시 저장
                false,
                "서울 올림픽공원",
                "http://maps.example.com",
                "http://apply.example.com",
                "010-1234-5678",
                "임시 저장 설명",
                List.of("#서울","#IT")
        );

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.eventId").isNumber())
                .andExpect(jsonPath("$.message").value("행사가 임시저장 되었습니다."))
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        Event savedEvent = eventRepository.findAll().get(0);
        assertThat(savedEvent.getStatus()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"OWNER"})
    void deleteEvent_Success_Test() throws Exception {
        Event event = eventRepository.save(createEvent("삭제용 test 엔티티"));

        mockMvc.perform(delete("/events/{id}", event.getId())) // DELETE 메서드 사용
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("행사가 삭제되었습니다."))
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        assertThat(eventRepository.existsById(event.getId())).isFalse();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"OWNER"})
    void updateEvent_Success_Test() throws Exception {
        Event event = eventRepository.save(createEvent("원본 제목"));

        EventRequest.UpdateEvent request = new EventRequest.UpdateEvent(
                "수정된 제목",
                "http://example.com/new-thumb.png",
                EventCategory.COMPETITION_HACKATHON,
                LocalDateTime.of(2025, 10, 1, 14, 0),
                LocalDateTime.of(2025, 10, 1, 16, 0),
                LocalDateTime.of(2025, 9, 15, 0, 0),
                LocalDateTime.of(2025, 9, 30, 23, 59),
                false,
                20000,
                List.of("PLANNER", "AI_DEVELOPER"),
                true,
                false,
                "서울 코엑스",
                "http://maps.example.com/new",
                "http://apply.example.com/new",
                "010-9876-5432",
                "완전히 수정된 이벤트 설명",
                List.of("#AI", "#워크숍")
        );
        mockMvc.perform(put("/events/{id}", event.getId()) // PUT 메서드 사용
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("행사가 수정되었습니다."))
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        Event updatedEvent = eventRepository.getEvent(event.getId());
        assertThat(updatedEvent.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedEvent.getThumbnailUrl()).isEqualTo("http://example.com/new-thumb.png");
        assertThat(updatedEvent.getCategory()).isEqualTo(EventCategory.COMPETITION_HACKATHON);
        assertThat(updatedEvent.getEventStart()).isEqualTo(LocalDateTime.of(2025, 10, 1, 14, 0));
        assertThat(updatedEvent.getEventEnd()).isEqualTo(LocalDateTime.of(2025, 10, 1, 16, 0));
        assertThat(updatedEvent.getRecruitStart()).isEqualTo(LocalDateTime.of(2025, 9, 15, 0, 0));
        assertThat(updatedEvent.getRecruitEnd()).isEqualTo(LocalDateTime.of(2025, 9, 30, 23, 59));
        assertThat(updatedEvent.getIsFree()).isFalse();
        assertThat(updatedEvent.getPrice()).isEqualTo(20000);
        assertThat(
                updatedEvent.getTargetRoles().stream()
                        .map(TargetRole::getName)
                        .collect(Collectors.toList())
        ).containsExactlyInAnyOrder("PLANNER", "AI_DEVELOPER");
        assertThat(updatedEvent.getIsOnline()).isFalse();
        assertThat(updatedEvent.getLocationText()).isEqualTo("서울 코엑스");
        assertThat(updatedEvent.getLocationLink()).isEqualTo("http://maps.example.com/new");
        assertThat(updatedEvent.getApplyLink()).isEqualTo("http://apply.example.com/new");
        assertThat(updatedEvent.getContact()).isEqualTo("010-9876-5432");
        assertThat(updatedEvent.getDescription()).isEqualTo("완전히 수정된 이벤트 설명");
        assertThat(
                updatedEvent.getHashTags().stream()
                        .map(HashTag::getName)
                        .collect(Collectors.toList())
        ).containsExactlyInAnyOrder("#AI" , "#워크숍");
        assertThat(updatedEvent.getStatus()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"OWNER"})
    void hideEvent_Success_Test() throws Exception {

        Event event = eventRepository.save(createEvent("숨김용 테스트 행사"));

        mockMvc.perform(patch("/events/{id}/hide", event.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("행사가 숨김 처리되었습니다."))
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        Event hiddenEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertThat(hiddenEvent.getStatus()).isEqualTo(EventStatus.HIDDEN);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"OWNER"})
    void publishEvent_Success_Test() throws Exception {

        Event event = createEvent("공개용 테스트 행사");

        event.setStatus(EventStatus.HIDDEN);
        Event hidden_event = eventRepository.save(event);

        mockMvc.perform(patch("/events/{id}/publish", hidden_event.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("행사가 공개되었습니다."))
                .andExpect(jsonPath("$.code").value("SUCCESS"));


        Event publishedEvent = eventRepository.findById(hidden_event.getId()).orElseThrow();
        assertThat(publishedEvent.getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    @DisplayName("카테고리로 행사 조회 페이징 테스트")
    void getEventBySearch_Paging_Test()
    {

        Event savedEvent =eventRepository.save(createEvent("저장"));
        Event savedEvent2 =eventRepository.save(createEvent("저장",EventCategory.CONFERENCE_SEMINAR));
        Event savedEvent3 =eventRepository.save(createEvent("저장",EventCategory.NETWORKING_MENTORING));

        for(int i=0;i<20;i++)
            eventRepository.save(createEvent("저장",EventCategory.CONFERENCE_SEMINAR));
        List<EventResponse.HomeEventResponse> resultByCategory
                = eventService.getEventBySearch
                (EventRequest.EventSearchCondition.builder().category(EventCategory.CONFERENCE_SEMINAR).sort("latest").page(0).build());

        List<EventResponse.HomeEventResponse> resultByCategory2
                = eventService.getEventBySearch
                (EventRequest.EventSearchCondition.builder().category(EventCategory.CONFERENCE_SEMINAR).sort("latest").page(1).build());

        assertNotNull(resultByCategory);
        assertEquals(12, resultByCategory.size());
        assertEquals(savedEvent.getTitle(), resultByCategory.get(0).getTitle());
        assertEquals(9, resultByCategory2.size());
    }

    @Test
    @DisplayName("카테고리로 행사 조회 정렬 테스트")
    void getEventBySearch_Popularity_Test() throws NoSuchFieldException, IllegalAccessException, InterruptedException {

        // 테스트용 이벤트 생성
        Event event1 = Event.builder()
                .title("테스트 이벤트1")
                .category(EventCategory.CONFERENCE_SEMINAR)
                .status(EventStatus.PUBLISHED)
                .isFree(true)
                .isOnline(true)
                .recruitStart(LocalDateTime.now().minusDays(20))
                .recruitEnd(LocalDateTime.now().plusDays(500))
                .eventStart(LocalDateTime.now().minusDays(20))
                .eventEnd(LocalDateTime.now().plusDays(20))
                .build();

        Event event2 = Event.builder()
                .title("테스트 이벤트2")
                .category(EventCategory.CONFERENCE_SEMINAR)
                .status(EventStatus.PUBLISHED)
                .isFree(true)
                .isOnline(true)
                .recruitStart(LocalDateTime.now().minusDays(20))
                .recruitEnd(LocalDateTime.now().plusDays(10))
                .eventStart(LocalDateTime.now().minusDays(20))
                .eventEnd(LocalDateTime.now().plusDays(20))
                .build();

        Event event3 = Event.builder()
                .title("테스트 이벤트3")
                .category(EventCategory.CONFERENCE_SEMINAR)
                .status(EventStatus.PUBLISHED)
                .isFree(true)
                .isOnline(true)
                .recruitStart(LocalDateTime.now().minusDays(20))
                .recruitEnd(LocalDateTime.now().plusDays(1000))
                .eventStart(LocalDateTime.now().minusDays(20))
                .eventEnd(LocalDateTime.now().plusDays(20))
                .build();


        eventRepository.save(event1);
        sleep(500);
        eventRepository.save(event2);
        sleep(500);
        eventRepository.save(event3);


        // event는 3달 이후 조회수 2개 + 3달 이내 조회수 1개
        EventViewDaily oldView = EventViewDaily.builder()
                .event(event1)
                .cnt(1000L)
                .build();
        eventViewDailyRepository.save(oldView);

        EventViewDaily oldView2 = EventViewDaily.builder()
                .event(event1)
                .cnt(100L)
                .build();
        eventViewDailyRepository.save(oldView2);

        EventViewDaily recentView = EventViewDaily.builder()
                .event(event1)
                .cnt(1L)
                .build();
        eventViewDailyRepository.save(recentView);

        //event2는 3달이내 조회수 2개
        EventViewDaily recentView2 = EventViewDaily.builder()
                .event(event2)
                .cnt(1L)
                .build();
        eventViewDailyRepository.save(recentView2);

        EventViewDaily recentView3 = EventViewDaily.builder()
                .event(event2)
                .cnt(1L)
                .build();
        eventViewDailyRepository.save(recentView3);

        EventAction action = EventAction.builder().event(event2).actorType(ActorType.USER).actionType(ActionType.VIEW).actorId("3L").build();
        EventAction action2 = EventAction.builder().event(event2).actorType(ActorType.USER).actionType(ActionType.APPLY).actorId("3L").build();

        eventActionRepository.save(action);
        eventActionRepository.save(action2);



        // 조건 DTO
        EventRequest.EventSearchCondition condPopularity = EventRequest.EventSearchCondition.builder()
                .category(EventCategory.CONFERENCE_SEMINAR)
                .sort("popularity")
                .page(0)
                .build();

        EventRequest.EventSearchCondition condLatest = EventRequest.EventSearchCondition.builder()
                .category(EventCategory.CONFERENCE_SEMINAR)
                .sort("latest")
                .page(0)
                .build();

        EventRequest.EventSearchCondition condDeadline = EventRequest.EventSearchCondition.builder()
                .category(EventCategory.CONFERENCE_SEMINAR)
                .sort("deadline")
                .page(0)
                .build();



        List<EventResponse.HomeEventResponse> resultsByPopularity = eventService.getEventBySearch(condPopularity);

        List<EventResponse.HomeEventResponse> resultsByLatest = eventService.getEventBySearch(condLatest);

        List<EventResponse.HomeEventResponse> resultsByDeadLine = eventService.getEventBySearch(condDeadline);

        // assertions
        assertThat(resultsByDeadLine).isNotEmpty();
        assertThat(resultsByDeadLine.get(0).getId()).isEqualTo(event2.getId());

        assertThat(resultsByLatest).isNotEmpty();
        assertThat(resultsByLatest.get(0).getId()).isEqualTo(event3.getId());

        assertThat(resultsByPopularity).isNotEmpty();
        assertThat(resultsByPopularity.get(0).getId()).isEqualTo(event1.getId());

        for(EventResponse.HomeEventResponse event :resultsByPopularity)
        {
            System.out.println(event.getRecommendedRate());
        }


        //created_at에 값을 채워서 저장해도 JPA가 now로 덮어 씌워서 creat_at을 조정하지는 못했네요.. 좋은 테스트 방법 있으면 추천 부탁드려요


    }

    @Test
    @DisplayName("행사 추천 기존 카테고리가 2개 이하여서 다른 카테고리로 보충 성공 테스트")
    public void getSupplementaryEvents_Success()
    {
        Event savedEvent1 =eventRepository.save(createEvent("저장",EventCategory.COMPETITION_HACKATHON));
        Event savedEvent2 =eventRepository.save(createEvent("저장",EventCategory.CONFERENCE_SEMINAR));
        Event savedEvent3 =eventRepository.save(createEvent("저장",EventCategory.NETWORKING_MENTORING));

        List<EventResponse.HomeEventResponse> result=eventService.getSupplementaryEvents(EventCategory.NETWORKING_MENTORING);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getId()).isEqualTo(savedEvent3.getId());
        assertThat(result.get(1).getId()).isEqualTo(savedEvent2.getId());
        assertThat(result.get(2).getId()).isEqualTo(savedEvent1.getId());

    }

    @Test
    @DisplayName("해시태그 기반으로 이벤트 추천 테스트")
    public void getRecommendedEvent_Success()
    {

        Event event1 = Event.builder()
                .title("테스트 이벤트1")
                .category(EventCategory.CONFERENCE_SEMINAR)
                .status(EventStatus.PUBLISHED)
                .isFree(true)
                .isOnline(true)
                .recruitStart(LocalDateTime.now().minusDays(20))
                .recruitEnd(LocalDateTime.now().plusDays(500))
                .eventStart(LocalDateTime.now().minusDays(20))
                .eventEnd(LocalDateTime.now().plusDays(20))
                .hashTags(new HashSet<>(List.of(hashTag, hashTag2, hashTag3, hashTag4)))
                .build();

        Event event2 = Event.builder()
                .title("테스트 이벤트2")
                .category(EventCategory.CONFERENCE_SEMINAR)
                .status(EventStatus.PUBLISHED)
                .isFree(true)
                .isOnline(true)
                .recruitStart(LocalDateTime.now().minusDays(20))
                .recruitEnd(LocalDateTime.now().plusDays(10))
                .eventStart(LocalDateTime.now().minusDays(20))
                .eventEnd(LocalDateTime.now().plusDays(20))
                .hashTags(new HashSet<>(List.of(hashTag3, hashTag4, hashTag5)))
                .build();

        Event event3 = Event.builder()
                .title("테스트 이벤트3")
                .category(EventCategory.CONFERENCE_SEMINAR)
                .status(EventStatus.PUBLISHED)
                .isFree(true)
                .isOnline(true)
                .recruitStart(LocalDateTime.now().minusDays(20))
                .recruitEnd(LocalDateTime.now().plusDays(1000))
                .eventStart(LocalDateTime.now().minusDays(20))
                .eventEnd(LocalDateTime.now().plusDays(20))
                .hashTags(new HashSet<>(List.of(hashTag6, hashTag7, hashTag3,hashTag2)))
                .build();

        eventRepository.saveAll(List.of(event1, event2, event3));

        EventAction action = EventAction.builder().event(event2).actorType(ActorType.USER).actionType(ActionType.VIEW).actorId("3L").build();

        eventActionRepository.save(action);

        List<EventResponse.HomeEventResponse> events=eventService.getRecommendedEvents(3L);
        assertThat(events).isNotEmpty();
        assertThat(events.size()).isEqualTo(2);
        for(EventResponse.HomeEventResponse event :events)
        {
            System.out.println(event.getTitle());
        }

    }



}
