package com.example.skillup.global.exception;

import com.example.skillup.global.common.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommonErrorCode implements ResultCode {
    DATA_NOT_FOUND("DATA_NOT_FOUND", "데이터를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INVALID_PASSWORD("INVALID_PASSWORD", "비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("ACCESS_DENIED", "권한이 없습니다.", HttpStatus.FORBIDDEN),
    INVALID_INPUT_VALUE("INVALID_INPUT_VALUE", "잘못된 입력값입니다.", HttpStatus.BAD_REQUEST),
    DATABASE_ERROR("DATABASE_ERROR", "데이터베이스에서 에러가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    REFRESH_TOKEN_EXPIRED("REFRESH_TOKEN_EXPIRED", "리프래쉬 토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_INVALID("REFRESH_TOKEN_INVALID", "리프레쉬 토큰이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
    FILE_UPLOAD_ERROR("FILE_UPLOAD_ERROR", "파일 업로드에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_READ_ERROR("FILE_READ_ERROR", "파일을 읽는 도중 오류가 발생했습니다.", HttpStatus.BAD_REQUEST),
    AWS_S3_SERVICE_ERROR("AWS_S3_SERVICE_ERROR", "S3 에 파일 업로드중 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    INVALID_FILE_TYPE("INVALID_FILE_TYPE", "지원하지 않는 파일 확장자 입니다.(jpg , png 만 가능합니다.)", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEED("FILE_SIZE_EXCEED", "파일 크기가 제한을 초과했습니다.", HttpStatus.BAD_REQUEST),
    ;


    private final String code;
    private final String message;
    private final HttpStatus status;
}
