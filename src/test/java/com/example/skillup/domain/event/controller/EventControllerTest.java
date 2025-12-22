package com.example.skillup.domain.event.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.skillup.domain.event.service.EventBannerService;
import com.example.skillup.domain.event.service.EventBookmarkService;
import com.example.skillup.domain.event.service.EventService;
import com.example.skillup.global.config.WebMvcConfig;
import com.example.skillup.global.interceptor.GuestIdInterceptor;
import com.example.skillup.global.search.service.EventSearchService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = EventController.class)
@Import({WebMvcConfig.class, GuestIdInterceptor.class})
@AutoConfigureMockMvc(addFilters = false)
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @MockitoBean
    private EventSearchService eventSearchService;

    @MockitoBean
    private EventBookmarkService eventBookmarkService;

    @MockitoBean
    private EventBannerService eventBannerService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMetamodelMappingContext;


    @Test
    @DisplayName("처음 방문(쿠키 없음) -> 인터셉터가 동작하여 쿠키 생성 후 서비스 호출")
    void getEventDetail_FirstVisit() throws Exception {
        // given
        Long eventId = 1L;
        given(eventService.getEventDetail(eq(eventId), any(), anyString()))
                .willReturn(null);

        // when & then
        mockMvc.perform(get("/events/{eventId}", eventId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(cookie().exists(GuestIdInterceptor.GUEST_COOKIE_NAME));
    }

    @Test
    @DisplayName("재방문(쿠키 있음) -> 기존 쿠키 값으로 서비스 호출")
    void getEventDetail_Revisit() throws Exception {
        // given
        String myGuestId = "my-uuid-1234";
        Cookie cookie = new Cookie(GuestIdInterceptor.GUEST_COOKIE_NAME, myGuestId);

        // when & then
        mockMvc.perform(get("/events/1")
                        .cookie(cookie))
                .andDo(print())
                .andExpect(status().isOk());
        verify(eventService).getEventDetail(any(), any(), eq(myGuestId));
    }
}