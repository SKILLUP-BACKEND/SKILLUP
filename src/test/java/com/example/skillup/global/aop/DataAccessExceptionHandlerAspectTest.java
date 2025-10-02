package com.example.skillup.global.aop;

import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.global.exception.CommonErrorCode;
import com.example.skillup.global.exception.GlobalExceptionHandler;
import org.aspectj.lang.ProceedingJoinPoint;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
class DataAccessExceptionHandlerAspectTest {

    @InjectMocks
    private DataAccessExceptionHandlerAspect aspect;

    @Mock
    private ProceedingJoinPoint joinPoint;

    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;

    @Test
    @DisplayName("DataAccessException 발생 시 EventException으로 변환됨")
    void testHandleDataAccessException() throws Throwable {
        // given
        when(joinPoint.proceed()).thenThrow(new DataAccessResourceFailureException("DB 연결 실패"));

        // when & then
        EventException ex = assertThrows(EventException.class,
                () -> aspect.handleExceptions(joinPoint));
        System.out.println(ex);
        System.out.println(globalExceptionHandler.handleMyException(ex));
        assertTrue(ex.getMessage().contains("DB 연결 실패"));
    }

    @Test
    @DisplayName("정상 동작 시 원래 결과 반환")
    void testProceedNormally() throws Throwable {
        // given
        when(joinPoint.proceed()).thenReturn("정상 결과");

        // when
        Object result = aspect.handleExceptions(joinPoint);

        // then
        assertEquals("정상 결과", result);
    }
}
