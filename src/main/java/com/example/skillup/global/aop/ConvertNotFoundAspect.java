package com.example.skillup.global.aop;

import com.example.skillup.global.common.ResultCode;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Aspect
@Component
public class ConvertNotFoundAspect {

    @Around("@annotation(convertNotFound)")
    public Object handle(ProceedingJoinPoint pjp,
                         ConvertNotFound convertNotFound) throws Throwable {

        try {
            return pjp.proceed();

        } catch (NoSuchElementException e) {
            Object[] args= pjp.getArgs();
            String targetValue =  args.length > 0 ? args[0].toString() : "";

            String message = convertNotFound.messageFormat()
                    .formatted(targetValue);

            Class<? extends ResultCode> enumClass = convertNotFound.errorCodeEnum();
            String name = convertNotFound.errorCodeName();

            ResultCode errorCode =  (ResultCode) Enum.valueOf((Class) enumClass, name);

            RuntimeException ex =
                    convertNotFound.exception()
                            .getConstructor(ResultCode.class, String.class)
                            .newInstance(errorCode, message);

            throw ex;
        }
    }
}