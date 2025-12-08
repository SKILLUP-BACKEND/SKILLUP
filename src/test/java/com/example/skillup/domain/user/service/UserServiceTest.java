package com.example.skillup.domain.user.service;


import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventBookmark;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.repository.EventBookmarkRepository;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.user.dto.request.UserRequest;
import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.Interest;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.enums.UserStatus;
import com.example.skillup.domain.user.repository.InterestRepository;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.common.BaseEntity;
import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;


@SpringBootTest
@ActiveProfiles("test")
public class UserServiceTest {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InterestRepository interestRepository;

    @Autowired
    private TargetRoleRepository targetRoleRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventBookmarkRepository eventBookmarkRepository;

    @MockitoBean
    private S3Service s3Service;

    private Users u1;
    private TargetRole role;

    @BeforeEach
    void setUp() {
        targetRoleRepository.deleteAll();
        userRepository.deleteAll();
        role = targetRoleRepository.save(TargetRole.builder().name("PLANNER").build());
        targetRoleRepository.save(TargetRole.builder().name("DESIGNER").build());
        targetRoleRepository.save(TargetRole.builder().name("AI_DEVELOPER").build());
        u1 = userRepository.save(
                Users.builder()
                        .email("seed1@ex.com")
                        .name("Seed1")
                        .gender("남")
                        .age("15")
                        .jobGroup("개발자")
                        .notificationFlag("Y")
                        .socialId("test")
                        .regDatetime(LocalDateTime.now())
                        .socialLoginType(SocialLoginType.google)
                        .lastLoginAt(LocalDateTime.now())
                        .status(UserStatus.ACTIVE)
                        .role(role)
                        .build()
        );
    }

    @Test
    public void getMyPageHome_Success() {
        UserResponse.MyPageHomeResponse response = userService.getMyPageHome(u1);
        assertThat(u1.getEmail()).isEqualTo(response.getEmail());
        assertThat(u1.getName()).isEqualTo(response.getName());

    }

    @Test
    public void getMyPageBookMark_Success() throws IllegalAccessException, NoSuchFieldException {

        Field createdField = BaseEntity.class.getDeclaredField("createdAt");
        createdField.setAccessible(true);

        Event event1 = Event.builder()
                .title("테스트 이벤트1")
                .category(EventCategory.CONFERENCE_SEMINAR)
                .status(EventStatus.PUBLISHED)
                .isFree(true)
                .isOnline(true)
                .recruitStart(LocalDateTime.now().minusDays(20))
                .recruitEnd(LocalDateTime.now().minusDays(20))
                .eventStart(LocalDateTime.now().minusDays(20))
                .eventEnd(LocalDateTime.now().minusDays(20))
                .build();

        Event event2 = Event.builder()
                .title("테스트 이벤트2")
                .category(EventCategory.CONFERENCE_SEMINAR)
                .status(EventStatus.PUBLISHED)
                .isFree(true)
                .isOnline(true)
                .recruitStart(LocalDateTime.now().minusDays(20))
                .recruitEnd(LocalDateTime.now().plusDays(100))
                .eventStart(LocalDateTime.now().minusDays(20))
                .eventEnd(LocalDateTime.now().plusDays(20))
                .build();

        Event event3 = Event.builder()
                .title("테스트 이벤트3")
                .category(EventCategory.BOOTCAMP_CLUB)
                .status(EventStatus.PUBLISHED)
                .isFree(true)
                .isOnline(true)
                .recruitStart(LocalDateTime.now().minusDays(20))
                .recruitEnd(LocalDateTime.now().plusDays(1000))
                .eventStart(LocalDateTime.now().minusDays(20))
                .eventEnd(LocalDateTime.now().plusDays(200))
                .build();

        Event event4 = Event.builder()
                .title("테스트 이벤트4")
                .category(EventCategory.CONFERENCE_SEMINAR)
                .status(EventStatus.PUBLISHED)
                .isFree(true)
                .isOnline(true)
                .recruitStart(LocalDateTime.now().minusDays(20))
                .recruitEnd(LocalDateTime.now().plusDays(10))
                .eventStart(LocalDateTime.now().minusDays(20))
                .eventEnd(LocalDateTime.now().plusDays(200))
                .build();

        eventRepository.saveAll(List.of(event1, event2, event3, event4));

        EventBookmark oldEventBookmark = EventBookmark.builder().event(event2).user(u1).build();
        createdField.set(oldEventBookmark, LocalDate.now().minusMonths(5).atStartOfDay());

        eventBookmarkRepository.save(oldEventBookmark);

        eventBookmarkRepository.save(EventBookmark.builder().event(event1).user(u1).build());
        eventBookmarkRepository.save(EventBookmark.builder().event(event3).user(u1).build());
        eventBookmarkRepository.save(EventBookmark.builder().event(event4).user(u1).build());

        UserResponse.MyPageBookMarkResponse response= userService.getMyPageBookMark(u1,"deadline",0);

        assertThat(u1.getEmail()).isEqualTo(response.getEmail());
        assertThat(response.getPageInfo().getCurrentPage()).isEqualTo(1);
        assertThat(response.getPageInfo().getTotalPages()).isEqualTo(1);
        assertThat(response.getPageInfo().getPageSize()).isEqualTo(9);
        assertThat(u1.getName()).isEqualTo(response.getName());
        assertThat(event1.getId()).isEqualTo(response.getClosedEvents().get(0).getId());
        assertThat(event4.getId()).isEqualTo(response.getRecruitingEvents().get(0).getId());
        assertThat(1).isEqualTo(response.getClosedEvents().size());
        assertThat(3).isEqualTo(response.getRecruitingEvents().size());

        UserResponse.MyPageBookMarkResponse response2= userService.getMyPageBookMark(u1,"latest",0);

        assertThat(event2.getId()).isEqualTo(response2.getRecruitingEvents().get(2).getId());




    }

    @Test
    public void updateUser_Success() {
        interestRepository.save(Interest.builder().role(role).name("코딩").build());
        UserRequest.UserUpdateRequest request = UserRequest.UserUpdateRequest.builder().age("22").name("김")
                .gender("남").role("AI_DEVELOPER").interests(List.of("코딩")).build();

        MultipartFile profileImage = new MockMultipartFile(
                "profileImage",
                "test-profile.jpg",
                "image/jpeg",
                "test image bytes".getBytes()
        );

        when(s3Service.uploadFile(any(MultipartFile.class), anyString())).thenReturn("test-profile.jpg");

        UserResponse.UserProfileResponse response = userService.updateUser(u1, request, profileImage);

        verify(s3Service).uploadFile(any(MultipartFile.class), anyString());

        assertThat(response.getProfileImageUrl()).endsWith("test-profile.jpg");
        assertThat(response.getAge()).isEqualTo("22");
        assertThat(response.getRole()).isEqualTo("AI_DEVELOPER");
        assertThat(response.getGender()).isEqualTo("남");
        assertThat(response.getInterests().get(0)).isEqualTo("코딩");
        assertThat(response.getName()).isEqualTo("김");

    }


}
