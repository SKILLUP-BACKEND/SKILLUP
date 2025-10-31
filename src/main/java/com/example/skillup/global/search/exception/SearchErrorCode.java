package com.example.skillup.global.search.exception;

import com.example.skillup.global.common.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum SearchErrorCode implements ResultCode {
    SEARCH_INDEXING_ERROR("SEARCH_INDEXING_ERROR", "행사 데이터를 인덱싱 처리 과정에서의 에러가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    SEARCH_SEARCH_QUERY_TOO_SHORT("SEARCH_SEARCH_QUERY_TOO_SHORT", "검색어는 2글자 이상 입력해 주세요.", HttpStatus.BAD_REQUEST),
    SEARCH_SEARCH_ERROR("SEARCH_SEARCH_ERROR", "검색 기능 시 에러가 발생했습니다.", HttpStatus.BAD_REQUEST),
    GROUP_ENTITY_NOT_FOUND("GROUP_ENTITY_NOT_FOUND","GRUOP 이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;
}
