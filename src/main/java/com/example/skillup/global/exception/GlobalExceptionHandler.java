package com.example.skillup.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

    public record ErrorResponse(String code, String message, String exception) {}


}
