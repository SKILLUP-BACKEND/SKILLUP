package com.example.skillup.domain.map.exception;

import com.example.skillup.global.common.ResultCode;
import com.example.skillup.global.exception.GlobalException;

public class MapException extends GlobalException {

    public MapException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public MapException(ResultCode resultCode) {
        super(resultCode, null);
    }
}
