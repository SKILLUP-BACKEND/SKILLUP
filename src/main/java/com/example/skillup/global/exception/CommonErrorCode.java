package com.example.skillup.global.exception;

import com.example.skillup.global.common.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ResultCode
{
    DATA_NOT_FOUND("DATA_NOT_FOUND", "데이터를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD("INVALID_PASSWORD", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("ACCESS_DENIED", "권한이 없습니다.", HttpStatus.FORBIDDEN),
    INVALID_INPUT_VALUE("INVALID_INPUT_VALUE", "잘못된 입력값입니다.", HttpStatus.BAD_REQUEST),
    DATABASE_ERROR("DATABASE_ERROR","데이터베이스에서 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;
}
