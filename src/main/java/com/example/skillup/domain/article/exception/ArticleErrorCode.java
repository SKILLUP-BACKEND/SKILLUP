package com.example.skillup.domain.article.exception;


import com.example.skillup.global.common.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ArticleErrorCode implements ResultCode {

    ARTICLE_ENTITY_NOT_FOUND("ARTICLE_ENTITY_NOT_FOUND","기사가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    INVALID_ARTICLE_SORT_TYPE("INVALID_ARTICLE_SORT_TYPE" , "올바르지 않은 sort 값입니다. " , HttpStatus.BAD_REQUEST),;

    private final String code;
    private final String message;
    private final HttpStatus status;
}
