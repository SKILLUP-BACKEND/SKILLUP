package com.example.skillup.domain.user.exception;


import com.example.skillup.global.exception.ErrorCode;
import com.example.skillup.global.exception.GlobalException;

public class UserException extends GlobalException
{
    public UserException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public UserException(ErrorCode errorCode) {
        super(errorCode, null);
    }
}
