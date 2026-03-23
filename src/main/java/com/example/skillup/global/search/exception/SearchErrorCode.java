package com.example.skillup.global.search.exception;

import com.example.skillup.global.common.ResultCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum SearchErrorCode implements ResultCode {
    SEARCH_INDEX_CREATE_ERROR("SEARCH_INDEX_CREATE_ERROR", "행사 데이터 인덱싱(생성/수정) 중 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    SEARCH_INDEX_DELETE_ERROR("SEARCH_INDEX_DELETE_ERROR", "행사 데이터 인덱스 삭제 중 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    SEARCH_INDEX_BULK_ERROR("SEARCH_INDEX_BULK_ERROR", "행사 데이터 벌크 인덱싱 중 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    SEARCH_INDEXING_ERROR("SEARCH_INDEXING_ERROR", "행사 데이터를 인덱싱 처리 과정에서의 에러가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    SEARCH_SEARCH_QUERY_TOO_SHORT("SEARCH_SEARCH_QUERY_TOO_SHORT", "검색어는 2글자 이상 입력해 주세요.", HttpStatus.BAD_REQUEST),
    SEARCH_SEARCH_ERROR("SEARCH_SEARCH_ERROR", "검색 서버와 통신 중 오류가 발생했습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    GROUP_ENTITY_NOT_FOUND("GROUP_ENTITY_NOT_FOUND", "그룹이 존재하지 않습니다.", HttpStatus.NOT_FOUND),
    SEARCH_DOCUMENT_SOURCE_NULL("SEARCH_DOCUMENT_SOURCE_NULL", "검색 결과 문서 데이터(_source)가 비어있습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    ;

    private final String code;
    private final String message;
    private final HttpStatus status;
}
