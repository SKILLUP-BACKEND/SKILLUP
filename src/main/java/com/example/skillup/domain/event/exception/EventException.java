package com.example.skillup.domain.event.exception;

import com.example.skillup.global.common.ResultCode;
import com.example.skillup.global.exception.GlobalException;

public class EventException extends GlobalException {

    public EventException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public EventException(ResultCode resultCode) {
        super(resultCode, null);
    }
}
