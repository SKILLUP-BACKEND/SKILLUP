package com.example.skillup.global.exception;

import com.example.skillup.global.exception.response.ValidationErrors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

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
                CommonErrorCode.ACCESS_DENIED.getCode(),
                CommonErrorCode.ACCESS_DENIED.getMessage(),
                ex.getClass().getSimpleName()
        );

        log.error("AccessDeniedException : {}", CommonErrorCode.ACCESS_DENIED.getMessage());

        return ResponseEntity.status(CommonErrorCode.ACCESS_DENIED.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrors> handleValidationException(MethodArgumentNotValidException ex) {
        List<ValidationErrors.FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationErrors.FieldErrorDetail(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();

        ValidationErrors response = new ValidationErrors(
                CommonErrorCode.INVALID_INPUT_VALUE.getCode(),
                fieldErrors,
                ex.getClass().getSimpleName()
        );

        return ResponseEntity.status(CommonErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(response);
    }

    public record ErrorResponse(String code, String message, String exception) {}

}
