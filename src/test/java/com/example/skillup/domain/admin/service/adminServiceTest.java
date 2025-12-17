package com.example.skillup.domain.admin.service;


import com.example.skillup.domain.admin.dto.AdminResponse;
import com.example.skillup.domain.event.entity.Event;
import com.example.skillup.domain.event.entity.EventAction;
import com.example.skillup.domain.event.entity.HashTag;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.event.enums.ActionType;
import com.example.skillup.domain.event.enums.ActorType;
import com.example.skillup.domain.event.enums.EventCategory;
import com.example.skillup.domain.event.enums.EventStatus;
import com.example.skillup.domain.event.repository.EventActionRepository;
import com.example.skillup.domain.event.repository.EventRepository;
import com.example.skillup.domain.event.repository.TargetRoleRepository;
import com.example.skillup.domain.oauth.Entity.SocialLoginType;
import com.example.skillup.domain.user.dto.response.UserResponse;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.enums.UserStatus;
import com.example.skillup.domain.user.exception.UserException;
import com.example.skillup.domain.user.repository.UserRepository;
import com.example.skillup.global.common.BaseEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest
@ActiveProfiles("test")
public class adminServiceTest
{
    @Autowired
    private AdminService adminService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TargetRoleRepository targetRoleRepository;
    @Autowired
    private EventRepository eventRepository;
    @Autowired
    private EventActionRepository eventActionRepository;
    TargetRole role;
    TargetRole role1;
    TargetRole role2;

    @BeforeEach
    void setup() throws NoSuchFieldException, IllegalAccessException {
        userRepository.deleteAll();
        role =targetRoleRepository.save(TargetRole.builder().name("개발자").build());
        role1 =targetRoleRepository.save(TargetRole.builder().name("디자이너").build());
        role2 =targetRoleRepository.save(TargetRole.builder().name("기획자").build());
        userRepository.save(
                Users.builder()
                        .email("seed1@ex.com")
                        .name("Seed1")
                        .gender("남")
                        .age("15")
                        .jobGroup("개발자")
                        .notificationFlag("Y")
                        .socialId("test12")
                        .regDatetime(LocalDateTime.now())
                        .socialLoginType(SocialLoginType.google)
                        .lastLoginAt(LocalDateTime.now())
                        .status(UserStatus.ACTIVE)
                        .role(role)
                        .build()
        );
        userRepository.save(
                Users.builder()
                        .email("seed2@ex.com")
                        .name("Seed1")
                        .gender("남")
                        .age("15")
                        .jobGroup("개발자")
                        .notificationFlag("Y")
                        .socialId("test3")
                        .regDatetime(LocalDateTime.now())
                        .socialLoginType(SocialLoginType.google)
                        .lastLoginAt(LocalDateTime.now())
                        .status(UserStatus.ACTIVE)
                        .role(role1)
                        .build()
        );
        userRepository.save(
                Users.builder()
                        .email("seed3@ex.com")
                        .name("Seed1")
                        .gender("남")
                        .age("15")
                        .jobGroup("개발자")
                        .notificationFlag("Y")
                        .socialId("test444")
                        .regDatetime(LocalDateTime.now())
                        .socialLoginType(SocialLoginType.google)
                        .lastLoginAt(LocalDateTime.now())
                        .status(UserStatus.ACTIVE)
                        .role(role2)
                        .build()
        );
        userRepository.save(
                Users.builder()
                        .email("seed4@ex.com")
                        .name("김철수")
                        .gender("남")
                        .age("15")
                        .jobGroup("개발자")
                        .notificationFlag("Y")
                        .socialId("test2")
                        .regDatetime(LocalDateTime.now())
                        .socialLoginType(SocialLoginType.google)
                        .lastLoginAt(LocalDateTime.now())
                        .status(UserStatus.ACTIVE)
                        .role(role2)
                        .build()
        );

        Users oldUsers=  Users.builder()
                .email("seed5@ex.com")
                .name("김철수")
                .gender("남")
                .age("15")
                .jobGroup("개발자")
                .notificationFlag("Y")
                .socialId("test2")
                .regDatetime(LocalDateTime.now())
                .socialLoginType(SocialLoginType.google)
                .lastLoginAt(LocalDateTime.now())
                .status(UserStatus.ACTIVE)
                .role(role2)
                .build();
        Field createdField = BaseEntity.class.getDeclaredField("deletedAt");
        createdField.setAccessible(true);

        createdField.set(oldUsers, LocalDate.now().minusMonths(5).atStartOfDay());
        userRepository.save(oldUsers);
    }


    @Test
    public void getUserBySearch_Success()
    {
        AdminResponse.AdminUserPageResponse response1 =adminService.getUsersBySearch(null,false);
        for(UserResponse.AdminUserResponse u : response1.getUsers())
        {
            System.out.println(u.getRole());
            System.out.println(u.getEmail());
            System.out.println(u.getName());
            System.out.println(u.getCreatedAt());
            System.out.println(u.getStatus());
            System.out.println(u.getSocialLoginType());
        }
        assertThat(4).isEqualTo(response1.getUsers().size());
        assertThat(2).isEqualTo(response1.getPmUsers().size());
        assertThat(1).isEqualTo(response1.getDevUsers().size());
        assertThat(1).isEqualTo(response1.getDesignerUsers().size());



        AdminResponse.AdminUserPageResponse response2 = adminService.getUsersBySearch("철",false);

        assertThat(1).isEqualTo(response2.getUsers().size());
        assertThat(1).isEqualTo(response2.getPmUsers().size());
        assertThat(0).isEqualTo(response2.getDevUsers().size());
        assertThat(0).isEqualTo(response2.getDesignerUsers().size());


        AdminResponse.AdminUserPageResponse response3 = adminService.getUsersBySearch("철",true);
        assertThat(1
        ).isEqualTo(response2.getUsers().size());

    }
    Event setUp() {
        return eventRepository.save(Event.builder()
                .title("테스트 이벤트")
                .category(EventCategory.BOOTCAMP_CLUB)
                .status(EventStatus.PUBLISHED)
                .isFree(true)
                .isOnline(true)
                .recruitStart(LocalDateTime.now().minusDays(20))
                .recruitEnd(LocalDateTime.now().plusDays(500))
                .eventStart(LocalDateTime.now().minusDays(20))
                .eventEnd(LocalDateTime.now().plusDays(20))
                        .targetRoles(Set.of(role, role1))
                .hashTags(
                        null
                )
                .build());
    }

    void setUpEa(String actorId, Event event, ActionType actionType){
         eventActionRepository.save(
                EventAction.builder()
                        .actorId(actorId)
                        .actorType(ActorType.USER)
                        .event(event)
                        .actionType(actionType)
                        .build()
        );
    }



    @Test
    public void getUsersDetail_Success()
    {
        UserResponse.AdminUserDetailPageResponse response1 = adminService.getUsersDetail(1L);

            System.out.println(response1.getRole());
            System.out.println(response1.getEmail());
            System.out.println(response1.getName());
            System.out.println(response1.getCreatedAt());
            System.out.println(response1.getLastLoginAt());
            System.out.println(response1.getSocialLoginType());

    }

    @Test
    public void getUsersDetail_Fail()
    {
        assertThrows(
                UserException.class,
                () -> adminService.getUsersDetail(6L)
        );
    }

    @Test
    public void getEventActionAnalytics_Success() throws NoSuchFieldException, IllegalAccessException {
        List<Event> events = new ArrayList<>();
        for(int i=0; i<10;i++)
            events.add(setUp());
        for(int i=0;i<3;i++)
        {
            for(int j=1;j<=5;j++)
            {
                String actorId = j +"L";
                for(int k=0;k<10;k++)
                {
                    setUpEa(actorId,events.get(k),ActionType.values()[i % 3]);
                }
            }
        }

        AdminResponse.eventActionAnalyticsResponse response1=adminService.getUserEventActionAnalytics("1L","VIEW");
        for(AdminResponse.eventActionMonthlyCountResponse a :response1.getEventActionMonthlyCountResponses())
        {
            System.out.println(a.getMonthLabels());
            System.out.println(a.getOthersMonthlyCount());
            System.out.println(a.getUserMonthlyCount());
        }
        for(AdminResponse.rolePercentageResponse a: response1.getRolePercentageResponses())
        {
            System.out.println(a.getRole());
            System.out.println(a.getPercentage());
        }



    }


}
