package com.example.skillup.global.aop;

import com.example.skillup.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.Optional;

@Aspect
@Component
@RequiredArgsConstructor
public class ThrowIfEmptyAspect {

    @Around("@annotation(throwIfEmpty)")
    public Object checkEmpty(ProceedingJoinPoint joinPoint, ThrowIfEmpty throwIfEmpty) throws Throwable {
        Object result = joinPoint.proceed();
        if ((result instanceof Collection<?> collection && collection.isEmpty())
                || result == null
                || (result instanceof Optional<?> optional && optional.isEmpty()))
        {
            Class<? extends RuntimeException> exClass = throwIfEmpty.exception();

                Constructor<? extends RuntimeException> ctor = exClass.getConstructor(CommonErrorCode.class);
                throw ctor.newInstance(throwIfEmpty.errorCode());
        }
    return result;
    }
}
