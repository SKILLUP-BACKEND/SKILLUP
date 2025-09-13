package com.example.skillup.domain.event.service;


import com.example.skillup.domain.event.dto.request.EventRequest;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
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

    @BeforeEach
    void setUp() {
        eventRepository.deleteAll();
        targetRoleRepository.deleteAll();
        targetRoleRepository.save(new TargetRole("PLANNER"));
        targetRoleRepository.save(new TargetRole("DESIGNER"));
        targetRoleRepository.save(new TargetRole("AI_DEVELOPER"));
    }

    private Event createEvent(String title) {
        Set<TargetRole> roles = targetRoleRepository.findAll().stream()
                .filter(r -> r.getName().equals("DESIGNER"))
                .collect(Collectors.toSet());

        return Event.builder()
                .title(title)
                .status(EventStatus.PUBLISHED)
                .eventStart(LocalDateTime.of(2025, 9, 12, 10, 0))
                .eventEnd(LocalDateTime.of(2025, 9, 12, 12, 0))
                .thumbnailUrl("http://example.com/thumb.png")
                .category(EventCategory.BOOTCAMP_CLUB)
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
                .hashtags("#test")
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
                "#스포츠 , #러닝"
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
                "#테스트"
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
                "#AI , #워크숍"
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
        assertThat(updatedEvent.getHashtags()).isEqualTo("#AI , #워크숍");
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

}
