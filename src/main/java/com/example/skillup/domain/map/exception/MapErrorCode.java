package com.example.skillup.domain.map.exception;

import com.example.skillup.global.common.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MapErrorCode implements ResultCode {

    MAP_GEOCODE_ERROR("MAP_GEOCODE_ERROR", "주소 → 좌표 변환(지오코딩) 실패", HttpStatus.INTERNAL_SERVER_ERROR),
    MAP_REVERSE_GEOCODE_ERROR("MAP_REVERSE_GEOCODE_ERROR", "좌표 → 주소 변환(리버스 지오코딩) 실패", HttpStatus.INTERNAL_SERVER_ERROR),

    INVALID_ADDRESS_QUERY("INVALID_ADDRESS_QUERY", "주소(query)가 비어있거나 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    COORDINATE_NOT_FOUND("COORDINATE_NOT_FOUND", "해당 주소로 좌표를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),

    MAP_API_CLIENT_ERROR("MAP_API_CLIENT_ERROR", "지도 API 요청 오류(4xx)", HttpStatus.BAD_REQUEST),
    MAP_API_SERVER_ERROR("MAP_API_SERVER_ERROR", "지도 API 서버 오류(5xx)", HttpStatus.BAD_GATEWAY),

    MAP_API_TIMEOUT("MAP_API_TIMEOUT", "지도 API 응답이 지연되었습니다.", HttpStatus.GATEWAY_TIMEOUT),
    MAP_API_RESPONSE_PARSE_ERROR("MAP_API_RESPONSE_PARSE_ERROR", "지도 API 응답 파싱에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus status;
}
