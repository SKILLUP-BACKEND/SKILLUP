package com.example.skillup.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorResponse> handleMyException(GlobalException ex) {
        log.error("Exception: {}", ex.getResultCode().getMessage());

        ErrorResponse response = new ErrorResponse(
                ex.getResultCode().getCode(),
                ex.getMessage(),
                ex.getClass().getSimpleName()
        );

        return ResponseEntity.status(ex.getResultCode().getStatus()).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse response = new ErrorResponse(
                ErrorCode.ACCESS_DENIED.getCode(),
                ErrorCode.ACCESS_DENIED.getMessage(),
                ex.getClass().getSimpleName()
        );

        System.out.println("권한 부족: " + ex.getMessage());

        return ResponseEntity.status(ErrorCode.ACCESS_DENIED.getStatus()).body(response);
    }

    public record ErrorResponse(String code, String message, String exception) {}

}
