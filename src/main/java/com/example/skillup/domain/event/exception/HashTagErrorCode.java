package com.example.skillup.domain.event.exception;

import com.example.skillup.global.common.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum HashTagErrorCode implements ResultCode {

    HAST_TAG_NOT_FOUND("HAST_TAG_NOT_FOUND", "맞는 추천 대상이 DB 에 존재하지 않습니다.", HttpStatus.NOT_FOUND);


    private final String code;
    private final String message;
    private final HttpStatus status;
}
