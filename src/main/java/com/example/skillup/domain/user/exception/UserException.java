package com.example.skillup.domain.user.exception;


import com.example.skillup.global.common.ResultCode;
import com.example.skillup.global.exception.GlobalException;

public class UserException extends GlobalException
{
    public UserException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public UserException(ResultCode resultCode) {
        super(resultCode, null);
    }
}
