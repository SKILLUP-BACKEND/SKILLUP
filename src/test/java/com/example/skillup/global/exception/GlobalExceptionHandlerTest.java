package com.example.skillup.global.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;


    @Test
    @DisplayName("GlobalException 발생 시 응답 확인")
    void testGlobalException() throws Exception {


        mockMvc.perform(get("/events/1")
                        .with(user("user").roles("USER")))
                .andDo(print());
    }

    @Test
    @DisplayName("GlobalException 발생 시 응답 확인")
    void apiSuccess() throws Exception {


        mockMvc.perform(get("/events/all")
                        .with(user("user").roles("USER")))
                .andDo(print());
    }

    @Test
    @DisplayName("MethodArgumentTypeMismatchException 발생 시 응답 확인")
    void testMethodArgumentTypeMismatchException() throws Exception {


        mockMvc.perform(get("/events?category=IVSSD")
                        .with(user("user").roles("USER")))
                .andDo(print());
    }

}
