package com.example.skillup.domain.event.service;

import com.example.skillup.domain.event.dto.response.EventResponse;
import com.example.skillup.domain.event.entity.TargetRole;
import com.example.skillup.domain.user.entity.Users;
import com.example.skillup.domain.user.entity.UsersDetails;
import com.example.skillup.global.service.NotFoundGuardService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ClosingSoonTest {


    @Autowired
    private EventService eventService;

    @MockitoBean
    private NotFoundGuardService notFoundGuardService;

    @Test
    void closingSoonApiTest() {

        // 1️⃣ UsersDetails mock
        UsersDetails userDetails = Mockito.mock(UsersDetails.class);
        Users user = Mockito.mock(Users.class);
        TargetRole role = Mockito.mock(TargetRole.class);

        // 2️⃣ mock 동작 정의
        Mockito.when(userDetails.getUser()).thenReturn(user);
        Mockito.when(user.getId()).thenReturn(1L);

        Mockito.when(notFoundGuardService.getUsersNative(1L))
                .thenReturn(user);

        Mockito.when(user.getRole()).thenReturn(role);
        Mockito.when(role.getName()).thenReturn("기획자");

        // 3️⃣ 서비스 호출
        EventResponse.featuredEventResponseList result =
                eventService.getClosingSoonEvents(20, userDetails);
    }
}
