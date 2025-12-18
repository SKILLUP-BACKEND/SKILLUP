package com.example.skillup.domain.article.exception;

import com.example.skillup.global.common.ResultCode;
import com.example.skillup.global.exception.GlobalException;

public class ArticleException extends GlobalException {
    public ArticleException(ResultCode resultCode, String message) {
        super(resultCode, message);
    }

    public ArticleException(ResultCode resultCode) {
        super(resultCode, null);
    }
}
