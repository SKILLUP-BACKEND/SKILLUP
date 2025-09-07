package com.example.skillup.global.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonResultCode implements ResultCode {

    SUCCESS("200", "성공적으로 처리되었습니다.", HttpStatus.OK)
    ;
    private final String code;
    private final String message;
    private final HttpStatus status;
}
