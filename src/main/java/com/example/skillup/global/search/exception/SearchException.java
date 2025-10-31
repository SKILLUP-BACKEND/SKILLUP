package com.example.skillup.global.search.exception;

import com.example.skillup.global.common.ResultCode;
import com.example.skillup.global.exception.GlobalException;

public class SearchException extends GlobalException {
    public SearchException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public SearchException(ResultCode resultCode) {
        super(resultCode, null);
    }
}
