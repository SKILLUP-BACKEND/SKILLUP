package com.example.skillup.global.exception;

import com.example.skillup.global.common.ResultCode;
import lombok.Getter;

@Getter
public class GlobalException extends RuntimeException{
    private final ResultCode resultCode;

    public GlobalException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.resultCode = resultCode;
    }

    public GlobalException(ResultCode resultCode,String message) {
        super(message != null ? message + " " + resultCode.getMessage()
                : resultCode.getMessage());
        this.resultCode = resultCode;
    }
}
