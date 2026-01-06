package com.example.skillup.domain.event.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventAction;
import com.example.skillup.domain.event.entity.EventViewDaily;
import com.example.skillup.domain.event.entity.HashTag;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.enums.ActionType;
import com.example.skillup.domain.event.enums.ActorType;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventSortType;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.enums.HashTagCategory;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.exception.HashTagErrorCode;
import com.example.skillup.domain.event.exception.TargetRoleErrorCode;
import com.example.skillup.domain.event.repository.EventActionRepository;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.event.repository.EventViewDailyRepository;
import com.example.skillup.domain.event.repository.HashTagRepository;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.entity.UsersDetails;
import com.example.skillup.domain.user.enums.UserStatus;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.common.BaseEntity;
import com.example.skillup.global.service.NotFoundGuardService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;


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
    private NotFoundGuardService notFoundGuardService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private TargetRoleRepository targetRoleRepository;

    @Autowired
    private EventViewDailyRepository eventViewDailyRepository;

    @Autowired
    private EventActionRepository eventActionRepository;
    @Autowired
    private HashTagRepository hashTagRepository;

    @Autowired
    private UserRepository userRepository;

    private HashTag hashTag;
    private HashTag hashTag2;
    private HashTag hashTag3;
    private HashTag hashTag4;
    private HashTag hashTag5;
    private HashTag hashTag6;
    private HashTag hashTag7;

    private TargetRole targetRole;

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        targetRoleRepository.deleteAll();
        targetRole = targetRoleRepository.save(TargetRole.builder().name("PLANNER").build());
        targetRoleRepository.save(TargetRole.builder().name("DESIGNER").build());
        targetRoleRepository.save(TargetRole.builder().name("AI_DEVELOPER").build());
        hashTag = hashTagRepository.save(HashTag.builder().category(HashTagCategory.EVENT_TYPE).name("#스포츠").build());
        hashTag2 = hashTagRepository.save(HashTag.builder().category(HashTagCategory.EVENT_TYPE).name("#러닝").build());
        hashTag3 = hashTagRepository.save(HashTag.builder().category(HashTagCategory.EVENT_TYPE).name("#서울").build());
        hashTag4 = hashTagRepository.save(HashTag.builder().category(HashTagCategory.EVENT_TYPE).name("#IT").build());
        hashTag5 = hashTagRepository.save(HashTag.builder().category(HashTagCategory.EVENT_TYPE).name("#AI").build());
        hashTag6 = hashTagRepository.save(HashTag.builder().category(HashTagCategory.EVENT_TYPE).name("#워크숍").build());
        hashTag7 = hashTagRepository.save(
                HashTag.builder().category(HashTagCategory.EVENT_TYPE).name("#PLANNER").build());

    }

    private Event createEvent(String title) {
        return createEvent(title, EventCategory.BOOTCAMP_CLUB); // 기본값
    }

    private Event createEvent(String title, EventCategory category) {
        Set<TargetRole> roles = targetRoleRepository.findAll().stream()
                .filter(r -> r.getName().equals("DESIGNER"))
                .collect(Collectors.toSet());
        roles.add(targetRoleRepository.findByName("AI_DEVELOPER").orElseThrow());
        Set<HashTag> tags = new HashSet<>();
        tags.add(hashTagRepository.findByName("#PLANNER").orElseThrow());

        return Event.builder()
                .title(title)
                .status(EventStatus.PUBLISHED)
                .eventStart(LocalDateTime.of(2025, 9, 12, 10, 0))
                .eventEnd(LocalDateTime.of(2026, 11, 12, 12, 0))
                .thumbnailUrl("http://example.com/thumb.png")
                .category(category) // 전달받은 값 사용
                .recruitEnd(LocalDateTime.of(2025, 9, 12, 12, 0))
                .recruitStart(LocalDateTime.of(2025, 9, 12, 10, 0))
                .isFree(false)
                .price(15000)
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

    private MockMultipartFile createThumbnailImage(String fileName) {
        return new MockMultipartFile("thumbnailImage", fileName, MediaType.IMAGE_JPEG_VALUE,
                "test-image-content".getBytes());
    }

    private MockMultipartFile createEventRequest(EventRequest.CreateEvent request) throws Exception {
        return new MockMultipartFile("request", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"OWNER"})
    void createEvent_Success_Test() throws Exception {

        EventRequest.CreateEvent request = new EventRequest.CreateEvent(
                "행사 생성 테스트",
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
                List.of("#스포츠", "#러닝")
        );

        MockMultipartFile thumbnail = createThumbnailImage("thumb.jpg");

        MockMultipartFile jsonPart = createEventRequest(request);

        mockMvc.perform(multipart("/events")
                        .file(thumbnail)
                        .file(jsonPart))
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
                List.of("#서울", "#IT")
        );

        MockMultipartFile thumbnail = createThumbnailImage("thumb.jpg");

        MockMultipartFile jsonPart = createEventRequest(request);

        mockMvc.perform(multipart("/events")
                        .file(jsonPart)
                        .file(thumbnail))
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

        EventRequest.CreateEvent request = new EventRequest.CreateEvent(
                "원본 제목",
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
                List.of("#스포츠", "#러닝")
        );

        MockMultipartFile thumbnail = createThumbnailImage("thumb.jpg");

        MockMultipartFile jsonPart = createEventRequest(request);

        MvcResult createResult = mockMvc.perform(multipart("/events")
                        .file(thumbnail)
                        .file(jsonPart))
                .andExpect(status().isOk())
                .andReturn();
        String createResponseBody = createResult.getResponse().getContentAsString();
        JsonNode createRoot = objectMapper.readTree(createResponseBody);

        long eventId = createRoot.path("data").path("eventId").asLong();

        System.out.println(eventId);

        EventRequest.UpdateEvent newRequest = new EventRequest.UpdateEvent(
                "수정된 제목",
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

        MockMultipartFile newThumbnail = createThumbnailImage("new.jpg");

        MockMultipartFile newJsonPart = new MockMultipartFile(
                "request",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(newRequest)
        );

        mockMvc.perform(multipart("/events/{id}", eventId) // PUT 메서드 사용
                        .file(newThumbnail)
                        .file(newJsonPart)
                        .with(r -> {
                            r.setMethod("PUT");
                            return r;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("행사가 수정되었습니다."))
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        Event updatedEvent = eventRepository.getEvent(eventId);
        assertThat(updatedEvent.getTitle()).isEqualTo("수정된 제목");
        assertThat(updatedEvent.getThumbnailUrl()).endsWith("new.jpg");
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
        ).containsExactlyInAnyOrder("#AI", "#워크숍");
        assertThat(updatedEvent.getStatus()).isEqualTo(EventStatus.DRAFT);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"OWNER"})
    void visibilityEvent_Success_Test() throws Exception {

        Event event = eventRepository.save(createEvent("숨김용 테스트 행사"));

        mockMvc.perform(patch("/events/{eventId}/visibility", event.getId())
                        .param("visibility", "false")
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

        mockMvc.perform(patch("/events/{eventId}/visibility", hidden_event.getId())
                        .param("visibility", "true")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("행사가 공개 처리되었습니다."))
                .andExpect(jsonPath("$.code").value("SUCCESS"));

        Event publishedEvent = eventRepository.findById(hidden_event.getId()).orElseThrow();
        assertThat(publishedEvent.getStatus()).isEqualTo(EventStatus.PUBLISHED);
    }

    @Test
    @DisplayName("카테고리로 행사 조회 페이징 테스트")
    void getEventBySearch_Paging_Test() {

        for (int i = 0; i < 20; i++) {
            eventRepository.save(createEvent("저장", EventCategory.CONFERENCE_SEMINAR));
        }
        EventResponse.SearchEventResponseList resultByCategory
                = eventService.getEventBySearch
                (EventRequest.EventSearchCondition.builder().category(EventCategory.CONFERENCE_SEMINAR)
                        .targetRole("DESIGNER").sort(EventSortType.LATEST).page(0).build());
        EventResponse.SearchEventResponseList resultByCategory2
                = eventService.getEventBySearch
                (EventRequest.EventSearchCondition.builder().category(EventCategory.CONFERENCE_SEMINAR)
                        .sort(EventSortType.LATEST)
                        .page(1).build());

        EventResponse.SearchEventResponseList resultByCategory3
                = eventService.getEventBySearch
                (EventRequest.EventSearchCondition.builder().category(EventCategory.CONFERENCE_SEMINAR)
                        .targetRole("PLANNER").sort(EventSortType.LATEST).page(0).build());

        assertThat(resultByCategory).isNotNull();
        System.out.println(resultByCategory.getTotal());

        assertEquals(12, resultByCategory.getHomeEventResponseList().size());
        assertEquals(2, resultByCategory.getPageInfoResponse().getTotalPages());
        assertEquals(1, resultByCategory.getPageInfoResponse().getCurrentPage());
        assertEquals(12, resultByCategory.getPageInfoResponse().getPageSize());

        assertEquals(8, resultByCategory2.getHomeEventResponseList().size());

        assertEquals(0, resultByCategory3.getHomeEventResponseList().size());
        assertEquals(0, resultByCategory3.getTotal());

    }

    @Test
    @DisplayName("카테고리로 행사 조회 정렬 테스트")
    void getEventBySearch_Popularity_Test() throws InterruptedException, NoSuchFieldException, IllegalAccessException {

        Field createdField = BaseEntity.class.getDeclaredField("createdAt");
        createdField.setAccessible(true);
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

        createdField.set(event1, LocalDate.now().minusMonths(2).atStartOfDay());
        createdField.set(event2, LocalDate.now().minusMonths(3).atStartOfDay());
        createdField.set(event3, LocalDate.now().minusMonths(1).atStartOfDay());

        eventRepository.save(event1);
        eventRepository.save(event2);
        eventRepository.save(event3);

        // event는 3달 이후 조회수 2개 + 3달 이내 조회수 1개
        EventViewDaily oldView = EventViewDaily.builder()
                .event(event1)
                .cnt(1L)
                .viewDate(LocalDate.now().minusMonths(4))
                .build();
        createdField.set(oldView, LocalDate.now().minusMonths(4).atStartOfDay());
        eventViewDailyRepository.save(oldView);

        EventViewDaily oldView2 = EventViewDaily.builder()
                .event(event1)
                .cnt(1L)
                .viewDate(LocalDate.now().minusMonths(5))
                .build();
        createdField.set(oldView, LocalDate.now().minusMonths(5).atStartOfDay());
        eventViewDailyRepository.save(oldView2);

        EventViewDaily recentView = EventViewDaily.builder()
                .event(event1)
                .cnt(1L)
                .viewDate(LocalDate.now())
                .build();
        eventViewDailyRepository.save(recentView);

        //event2는 3달이내 조회수 2개
        EventViewDaily recentView2 = EventViewDaily.builder()
                .event(event2)
                .cnt(1L)
                .viewDate(LocalDate.now())
                .build();
        eventViewDailyRepository.save(recentView2);

        EventViewDaily recentView3 = EventViewDaily.builder()
                .event(event2)
                .cnt(1L)
                .viewDate(LocalDate.now().minusDays(1))
                .build();
        eventViewDailyRepository.save(recentView3);

        EventAction action = EventAction.builder().event(event2).actorType(ActorType.USER).actionType(ActionType.VIEW)
                .actorId("3L").build();
        EventAction action2 = EventAction.builder().event(event2).actorType(ActorType.USER).actionType(ActionType.APPLY)
                .actorId("3L").build();

        eventActionRepository.save(action);
        eventActionRepository.save(action2);

        // 조건 DTO
        EventRequest.EventSearchCondition condPopularity = EventRequest.EventSearchCondition.builder()
                .category(EventCategory.CONFERENCE_SEMINAR)
                .sort(EventSortType.POPULARITY)
                .page(0)
                .build();

        EventRequest.EventSearchCondition condLatest = EventRequest.EventSearchCondition.builder()
                .category(EventCategory.CONFERENCE_SEMINAR)
                .sort(EventSortType.LATEST)
                .page(0)
                .build();

        EventRequest.EventSearchCondition condDeadline = EventRequest.EventSearchCondition.builder()
                .category(EventCategory.CONFERENCE_SEMINAR)
                .sort(EventSortType.DEADLINE)
                .page(0)
                .build();

        EventResponse.SearchEventResponseList resultsByPopularity = eventService.getEventBySearch(condPopularity);

        EventResponse.SearchEventResponseList resultsByLatest = eventService.getEventBySearch(condLatest);

        EventResponse.SearchEventResponseList resultsByDeadLine = eventService.getEventBySearch(condDeadline);

        // assertions
        assertThat(resultsByDeadLine).isNotNull();
        assertThat(resultsByDeadLine.getHomeEventResponseList().get(0).getId()).isEqualTo(event2.getId());
        assertThat(resultsByDeadLine.getTotal()).isEqualTo(resultsByPopularity.getTotal());

        assertThat(resultsByLatest).isNotNull();
        assertThat(resultsByLatest.getHomeEventResponseList().get(0).getId()).isEqualTo(event3.getId());

        assertThat(resultsByPopularity).isNotNull();
        assertThat(resultsByPopularity.getHomeEventResponseList().get(0).getId()).isEqualTo(event2.getId());

        for (EventResponse.HomeEventResponse event : resultsByPopularity.getHomeEventResponseList()) {
            System.out.println(event.getRecommendedRate());
        }

        System.out.println(resultsByLatest.getTotal());


    }

    @Test
    @DisplayName("행사 추천 기존 카테고리가 2개 이하여서 다른 카테고리로 보충 성공 테스트")
    public void getSupplementaryEvents_Success() {
        Event savedEvent1 = eventRepository.save(createEvent("저장", EventCategory.COMPETITION_HACKATHON));
        Event savedEvent2 = eventRepository.save(createEvent("저장", EventCategory.CONFERENCE_SEMINAR));
        Event savedEvent3 = eventRepository.save(createEvent("저장", EventCategory.NETWORKING_MENTORING));

        List<EventResponse.HomeEventResponse> result = eventService.getSupplementaryEvents(
                EventCategory.NETWORKING_MENTORING);

        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getId()).isEqualTo(savedEvent3.getId());
        assertThat(result.get(1).getId()).isEqualTo(savedEvent2.getId());
        assertThat(result.get(2).getId()).isEqualTo(savedEvent1.getId());

    }

    @Test
    @DisplayName("해시태그 기반으로 이벤트 추천 테스트")
    public void getRecommendedEvent_Success() {

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
                .hashTags(new HashSet<>(List.of(hashTag6, hashTag7, hashTag3, hashTag2)))
                .build();

        eventRepository.saveAll(List.of(event1, event2, event3));

        EventAction action = EventAction.builder().event(event2).actorType(ActorType.USER).actionType(ActionType.VIEW)
                .actorId("3L").build();

        eventActionRepository.save(action);

        List<EventResponse.HomeEventResponse> events = eventService.getRecommendedEvents(3L);
        assertThat(events).isNotEmpty();
        assertThat(events.size()).isEqualTo(2);
        for (EventResponse.HomeEventResponse event : events) {
            System.out.println(event.getTitle());
        }

    }

    @Test
    @DisplayName("getRole 실패 테스트")
    public void getTargetRoleByName_Fail() {
        EventException exception =
                assertThrows(EventException.class, () -> notFoundGuardService.getRole("잘못된 이름"));
        System.out.println(exception.getMessage());
        System.out.println(exception.getResultCode());
        assertEquals(TargetRoleErrorCode.TARGET_ROLE_NOT_FOUND, exception.getResultCode());
    }

    @Test
    @DisplayName("getRole 성공 테스트")
    public void getTargetRoleByName_Success() {
        TargetRole targetRole = notFoundGuardService.getRole("PLANNER");
        assertEquals("PLANNER", targetRole.getName());
    }

    @Test
    @DisplayName("getHashTag 실패 테스트")
    public void getHashTagByName_Fail() {
        EventException exception =
                assertThrows(EventException.class, () -> notFoundGuardService.getHashTag("잘못된 이름"));
        System.out.println(exception.getMessage());
        System.out.println(exception.getResultCode());
        assertEquals(HashTagErrorCode.HASH_TAG_NOT_FOUND, exception.getResultCode());
    }

    @Test
    @DisplayName("getHashTag 성공 테스트")
    public void getHashTagByName_Success() {
        HashTag targetRole = notFoundGuardService.getHashTag("#스포츠");
        assertEquals("#스포츠", targetRole.getName());
    }

    @Test
    @DisplayName("일반유저 및 비회원 행사 조회 및 조회수 , 최근기록 성공 테스트")
    public void getEventDetail_User_Success() {
        Users u1 = userRepository.save(
                Users.builder()
                        .email("seed1@ex.com")
                        .name("Seed1")
                        .gender("남")
                        .age("15")
                        .notificationFlag("Y")
                        .socialId("test")
                        .regDatetime(LocalDateTime.now())
                        .socialLoginType(SocialLoginType.google)
                        .lastLoginAt(LocalDateTime.now())
                        .status(UserStatus.ACTIVE)
                        .role(targetRole)
                        .build()
        );

        Event savedEvent = createEvent("test");

        eventRepository.save(savedEvent);

        UsersDetails principal = new UsersDetails(u1);

        EventResponse.EventSelectResponse result =
                eventService.getEventDetail(savedEvent.getId(), principal, null);

        assertThat(result).isNotNull();

        Event reloadEvent = eventRepository.getEvent(result.getId());

        assertThat(reloadEvent.getViewsCount()).isEqualTo(1L);

        Optional<EventViewDaily> eventViewDaily = eventViewDailyRepository.findByEventAndViewDate(reloadEvent,
                LocalDate.now());
        assertThat(eventViewDaily).isPresent();
        assertThat(eventViewDaily.get().getCnt()).isEqualTo(1L);

        Optional<EventAction> EventAction =
                eventActionRepository.findByEventAndActorIdAndActionType(reloadEvent,
                        principal.getUser().getId().toString(), ActionType.VIEW);
        assertThat(EventAction).isPresent();
        assertThat(EventAction.get().getActorType()).isEqualTo(ActorType.USER);

        String guestId = "guest-123";

        EventResponse.EventSelectResponse result2 =
                eventService.getEventDetail(result.getId(), null, guestId);

        assertThat(result2).isNotNull();
        reloadEvent = eventRepository.getEvent(result2.getId());

        assertThat(reloadEvent.getViewsCount()).isEqualTo(2L);
        eventViewDaily = eventViewDailyRepository.findByEventAndViewDate(reloadEvent, LocalDate.now());
        assertThat(eventViewDaily).isPresent();
        assertThat(eventViewDaily.get().getCnt()).isEqualTo(2L);

        EventAction =
                eventActionRepository.findByEventAndActorIdAndActionType(reloadEvent, guestId, ActionType.VIEW);
        assertThat(EventAction).isPresent();
        assertThat(EventAction.get().getActorType()).isEqualTo(ActorType.GUEST);
    }

    @Test
    @DisplayName("행사 신청 시 EventAction 이 존재하지 않으면 신청 수 1 증가하고 EventAction 을 만든다. 성공 테스트")
    void applyEventUser_Success() {
        Event event = eventRepository.save(createEvent("저장"));

        Users user = Users.builder()
                .id(1L)
                .email("test@example.com")
                .name("Seed1")
                .gender("남")
                .age("15")
                .notificationFlag("Y")
                .socialId("test")
                .regDatetime(LocalDateTime.now())
                .socialLoginType(SocialLoginType.google)
                .lastLoginAt(LocalDateTime.now())
                .status(UserStatus.ACTIVE)
                .role(targetRole)
                .build();

        UsersDetails usersDetails = new UsersDetails(user);

        EventResponse.EventApplyResponse eventApplyResponse = eventService.applyEvent(event.getId(), usersDetails,
                null);

        assertThat(eventApplyResponse).isNotNull();
        assertThat(eventApplyResponse.getComment()).isEqualTo("성공적으로 신청되었습니다.");

        Event newEvent = eventRepository.getEvent(event.getId());
        Optional<EventAction> eventAction = eventActionRepository.findByEventAndActorIdAndActionType(newEvent,
                user.getId().toString(), ActionType.APPLY);

        assertThat(eventAction).isPresent();
        assertThat(eventAction.get().getActorType()).isEqualTo(ActorType.USER);
        assertThat(eventAction.get().getActionType()).isEqualTo(ActionType.APPLY);

        assertThat(newEvent.getApplyClicks()).isEqualTo(1L);

    }

    @Test
    @DisplayName("행사 신청 시 EventAction 이 존재하면 신청 수 증가하지않고 반환. 성공 테스트")
    void applyEventDuplicateUser_Success() {
        Event event = eventRepository.save(createEvent("저장"));

        Users user = Users.builder()
                .id(1L)
                .email("test@example.com")
                .name("Seed1")
                .gender("남")
                .age("15")
                .notificationFlag("Y")
                .socialId("test")
                .regDatetime(LocalDateTime.now())
                .socialLoginType(SocialLoginType.google)
                .lastLoginAt(LocalDateTime.now())
                .status(UserStatus.ACTIVE)
                .role(targetRole)
                .build();

        UsersDetails usersDetails = new UsersDetails(user);

        eventActionRepository.save(
                EventAction.builder()
                        .actionType(ActionType.APPLY)
                        .actorId(user.getId().toString())
                        .actorType(ActorType.USER)
                        .event(event)
                        .build()
        );

        EventResponse.EventApplyResponse eventApplyResponse = eventService.applyEvent(event.getId(), usersDetails,
                null);

        assertThat(eventApplyResponse).isNotNull();
        assertThat(eventApplyResponse.getComment()).isEqualTo("이미 신청한 이력이 있습니다. 정상적으로 처리 되었습니다.");

        Event newEvent = eventRepository.getEvent(event.getId());

        List<EventAction> actions =
                eventActionRepository.findAllByEventAndActionType(event, ActionType.APPLY);

        assertThat(actions).hasSize(1);

        assertThat(newEvent.getApplyClicks()).isEqualTo(0L);

    }

}
