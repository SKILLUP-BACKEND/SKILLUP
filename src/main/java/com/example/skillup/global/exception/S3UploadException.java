package com.example.skillup.global.exception;

import com.example.skillup.global.common.ResultCode;

public class S3UploadException extends GlobalException {

    public S3UploadException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public S3UploadException(ResultCode resultCode) {
        super(resultCode, null);
    }
}
