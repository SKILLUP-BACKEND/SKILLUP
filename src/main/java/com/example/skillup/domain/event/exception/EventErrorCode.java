package com.example.skillup.domain.event.exception;

import com.example.skillup.global.common.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum EventErrorCode implements ResultCode {
    EVENT_ENTITY_NOT_FOUND("EVENT_ENTITY_NOT_FOUND","행사가 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    EVENT_ALREADY_HIDDEN("EVENT_ALREADY_HIDDEN", "이미 숨김된 행사입니다.", HttpStatus.BAD_REQUEST),
    EVENT_ALREADY_PUBLISHED("EVENT_ALREADY_PUBLISHED","이미 등록된 행사입니다." ,HttpStatus.BAD_REQUEST),
    EVENT_ALREADY_DELETED("EVENT_ALREADY_DELETED", "이미 삭제된 행사입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
