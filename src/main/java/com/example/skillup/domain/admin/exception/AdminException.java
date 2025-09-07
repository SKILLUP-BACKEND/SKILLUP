package com.example.skillup.domain.admin.exception;

import com.example.skillup.global.common.ResultCode;
import com.example.skillup.global.exception.GlobalException;

public class AdminException extends GlobalException
{
    public AdminException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public AdminException(ErrorCode errorCode) {
        super(errorCode, null);
    }
}
