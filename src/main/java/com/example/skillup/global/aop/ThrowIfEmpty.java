package com.example.skillup.global.aop;

import com.example.skillup.global.exception.ErrorCode;
import com.example.skillup.global.exception.GlobalException;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ThrowIfEmpty {
    Class<? extends RuntimeException> exception() default GlobalException.class;
    ErrorCode errorCode() default ErrorCode.DATA_NOT_FOUND;
}
