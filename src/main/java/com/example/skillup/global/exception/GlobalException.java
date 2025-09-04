package com.example.skillup.global.exception;

import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException{
    private final ErrorCode errorCode;

    public GlobalException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public GlobalException(ErrorCode errorCode,String message) {
        super(message != null ? message + " " + errorCode.getMessage()
                : errorCode.getMessage());
        this.errorCode = errorCode;
    }
}
