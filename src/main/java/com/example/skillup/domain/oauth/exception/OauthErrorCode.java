package com.example.skillup.domain.oauth.exception;

import com.example.skillup.global.common.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OauthErrorCode implements ResultCode
{
    NAVER_TOKEN_ERROR("NAVER_TOKEN_ERROR", "네이버 액세스 토큰 발급 실패", HttpStatus.UNAUTHORIZED),
    NAVER_SERVER_ERROR("NAVER_SERVER_ERROR", "네이버 서버 오류", HttpStatus.INTERNAL_SERVER_ERROR),
    UNSUPPORTED_SOCIAL_TYPE("UNSUPPORTED_SOCIAL_TYPE","지원하지 않는 소셜 타입",HttpStatus.BAD_REQUEST),
    FAIL_GET_ACCESS_TOKEN("FAIL_GET_ACCESS_TOKEN","액세스 토큰을 가져오기 실패",HttpStatus.INTERNAL_SERVER_ERROR),
    FAIL_GET_USER_INFO("FAIL_GET_USER_INFO","사용자 정보를 가져오기 실패", HttpStatus.INTERNAL_SERVER_ERROR);


    private final String code;
    private final String message;
    private final HttpStatus status;
}
