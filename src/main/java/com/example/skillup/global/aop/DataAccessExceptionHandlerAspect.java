package com.example.skillup.global.aop;

import com.example.skillup.domain.event.exception.EventException;
import com.example.skillup.global.exception.CommonErrorCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class DataAccessExceptionHandlerAspect {

    @Around("@annotation(com.example.skillup.global.aop.HandleDataAccessException)")
    public Object handleExceptions(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (DataAccessException e) {
            throw new EventException(CommonErrorCode.DATABASE_ERROR, e.getMessage());
        }
    }
}
