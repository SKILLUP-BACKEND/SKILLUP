package com.example.skillup.global.aop;


import com.example.skillup.global.common.ResultCode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ConvertNotFound {

    Class<? extends RuntimeException> exception();

    Class<? extends ResultCode> errorCodeEnum();

    String errorCodeName();

    String messageFormat() default "%s에";
}