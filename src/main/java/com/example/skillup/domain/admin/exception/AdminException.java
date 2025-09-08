package com.example.skillup.domain.admin.exception;

import com.example.skillup.global.common.ResultCode;
import com.example.skillup.global.exception.GlobalException;

public class AdminException extends GlobalException
{
    public AdminException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public AdminException(ResultCode resultCode) {
        super(resultCode, null);
    }
}
