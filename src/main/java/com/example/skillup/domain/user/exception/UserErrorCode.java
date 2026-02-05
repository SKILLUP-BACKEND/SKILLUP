package com.example.skillup.domain.user.exception;

import com.example.skillup.global.common.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ResultCode
{
    USER_ENTITY_NOT_FOUND("USER_ENTITY_NOT_FOUND","유저가 존재하지 않습니다", HttpStatus.NOT_FOUND),
    RECENT_SEARCH_ENTITY_NOT_FOUND("RECENT_SEARCH_ENTITY_NOT_FOUND","검색기록이 존재하지 않습니다", HttpStatus.NOT_FOUND),
    RECENT_SEARCH_FORBIDDEN("RECENT_SEARCH_FORBIDDEN", "해당 검색기록에 대한 권한이 없습니다", HttpStatus.FORBIDDEN),;


    private final String code;
    private final String message;
    private final HttpStatus status;
}
