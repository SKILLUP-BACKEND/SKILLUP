package com.example.skillup.domain.event.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventBookmark;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.domain.event.repository.EventBookmarkRepository;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.entity.UsersDetails;
import com.example.skillup.domain.user.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
public class EventBookmarkServiceTest {

    @InjectMocks
    private EventBookmarkService eventBookmarkService;

    @Mock
    private EventBookmarkRepository eventBookmarkRepository;

    @Mock
    private EventRepository eventRepository;


    LocalDateTime now = LocalDateTime.now();
    private Users user;
    private UsersDetails usersDetails;
    private Event event;

    @BeforeEach
    void setUp() {
        user = Users.builder()
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
                .role(TargetRole.builder().name("PLANNER").build())
                .build();

        usersDetails = new UsersDetails(user);

        event = Event.builder()
                .title("🔥 실무형 백엔드 부트캠프")
                .category(EventCategory.BOOTCAMP_CLUB)
                .status(EventStatus.PUBLISHED)
                .recruitStart(now.minusDays(10))
                .recruitEnd(now.plusDays(10))
                .eventStart(now.plusDays(20))
                .eventEnd(now.plusDays(40))
                .isOnline(false)
                .isFree(true)
                .price(0)
                .thumbnailUrl("https://example.com/thumb/backend.jpg")
                .applyClicks(120L)
                .viewsCount(0L)
                .bookmarkedCount(0L)
                .applyLink("https://example.com/apply/backend")
                .locationText("서울 강남")
                .locationLink("https://maps.example.com/abc")
                .contact("admin@example.com")
                .recommendedManual(false)
                .ad(false)
                .description("실무형 백엔드 집중 과정")
                .build();
    }

    @Test
    @DisplayName("비로그인 사용자는 북마크를 사용할 수 없습니다. user == null 이면 예외 발생")
    void updateBookmarked_fail_UserIsNULL() {
        Long eventId = 1L;

        assertThatThrownBy(() ->
                eventBookmarkService.updateBookmarked(null, eventId)
        )
                .isInstanceOf(EventException.class)
                .hasMessageContaining("북마크 기능은 로그인한 일반 사용자만 사용 가능합니다.");

    }

    @Test
    @DisplayName("기존 북마크가 있으면 updateBookmarked()가 호출되고, 해당 엔티티가 그대로 반환된다")
    void updateBookmarked_success_toggleExistingBookmark() {
        Long eventId = 1L;

        when(eventRepository.getEvent(eventId)).thenReturn(event);

        EventBookmark bookmark = EventBookmark.builder()
                .id(10L)
                .user(user)
                .event(event)
                .isBookmarked(true)
                .build();

        when(eventBookmarkRepository.findByUserAndEvent(user, event))
                .thenReturn(Optional.of(bookmark));

        EventBookmark result = eventBookmarkService.updateBookmarked(usersDetails, eventId);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getIsBookmarked()).isFalse();  // true -> false 로 바뀌었다고 가정

        // save는 호출되지 않아야 함
        verify(eventBookmarkRepository, never()).save(any(EventBookmark.class));
    }

    @Test
    @DisplayName("기존 북마크가 없으면 새 북마크를 생성하고 저장한다")
    void updateBookmarked_success_createNewBookmark() {
        // given
        Long eventId = 1L;

        when(eventRepository.getEvent(eventId)).thenReturn(event);
        when(eventBookmarkRepository.findByUserAndEvent(user, event))
                .thenReturn(Optional.empty());

        when(eventBookmarkRepository.save(any(EventBookmark.class)))
                .thenAnswer(invocation -> {
                    EventBookmark eventBookmark = invocation.getArgument(0);
                    ReflectionTestUtils.setField(eventBookmark, "id", 100L);
                    return eventBookmark;
                });

        EventBookmark result = eventBookmarkService.updateBookmarked(usersDetails, eventId);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getUser()).isEqualTo(user);
        assertThat(result.getEvent()).isEqualTo(event);
        assertThat(result.getIsBookmarked()).isTrue();

        verify(eventBookmarkRepository).save(any(EventBookmark.class));
    }
}
