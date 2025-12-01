package com.example.skillup.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class TimeTrackerAspect {

    @Around(
            "@within(org.springframework.web.bind.annotation.RestController) || " +
                    "@within(org.springframework.stereotype.Service)"
    )
    public Object measure(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();

        Object result = joinPoint.proceed();

        long end = System.currentTimeMillis();
        log.info("[TIME] {} 실행 시간 = {} ms",
                joinPoint.getSignature().toShortString(),
                (end - start));
        return result;
    }
}
