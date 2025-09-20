package com.example.skillup.domain.oauth.exception;

import com.example.skillup.global.common.ResultCode;
import com.example.skillup.global.exception.GlobalException;

public class OauthException extends GlobalException {

    public OauthException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public OauthException(ResultCode resultCode) {
        super(resultCode, null);
    }
}

