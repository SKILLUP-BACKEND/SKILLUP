package com.example.skillup.global.exception;

import com.example.skillup.global.common.BaseResponse;
import com.example.skillup.global.exception.response.ValidationErrors;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler
{
    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<BaseResponse<String>> handleMyException(GlobalException ex) {
        log.error("Exception: {}", ex.getMessage());

        BaseResponse<String> response = BaseResponse.error(ex.getResultCode(), ex.getMessage());
        return ResponseEntity.status(ex.getResultCode().getStatus()).body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<BaseResponse<Void>> handleAccessDeniedException(AccessDeniedException ex) {
        log.error("AccessDeniedException: {}", ex.getMessage());

        BaseResponse<Void> response = BaseResponse.error(CommonErrorCode.ACCESS_DENIED);
        return ResponseEntity.status(CommonErrorCode.ACCESS_DENIED.getStatus()).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<BaseResponse<ValidationErrors>> handleValidationException(MethodArgumentNotValidException ex) {
        List<ValidationErrors.FieldErrorDetail> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ValidationErrors.FieldErrorDetail(
                        error.getField(),
                        error.getDefaultMessage()
                ))
                .toList();
        fieldErrors.forEach(error ->
                log.error("Validation error - field: {}, message: {}", error.getField(), error.getMessage())
        );

        ValidationErrors validationErrors = new ValidationErrors(
                fieldErrors,
                ex.getClass().getSimpleName()
        );

        BaseResponse<ValidationErrors> response = BaseResponse.error(CommonErrorCode.INVALID_INPUT_VALUE, validationErrors);

        return ResponseEntity.status(CommonErrorCode.INVALID_INPUT_VALUE.getStatus())
                .body(response);
    }



    @ExceptionHandler({
            HttpMessageNotReadableException.class,      // JSON 깨짐
            MethodArgumentTypeMismatchException.class,  // enum, 숫자 타입 오류
            HttpMessageConversionException.class        // Jackson 변환 계열 최상위
    })
    public ResponseEntity<BaseResponse<Void>> handleRequestConvertException(Exception ex) {
        log.warn("Request conversion error", ex);

        return ResponseEntity
                .badRequest()
                .body(BaseResponse.error(CommonErrorCode.INVALID_INPUT_VALUE));
    }

    /* =========================
     * 필수 RequestParam 누락
     * ========================= */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<BaseResponse<Void>> handleMissingParameter(
            MissingServletRequestParameterException ex
    ) {
        log.warn("Missing request parameter: {}", ex.getParameterName());

        return ResponseEntity
                .badRequest()
                .body(BaseResponse.error(CommonErrorCode.INVALID_INPUT_VALUE));
    }

    /* =========================
     * @RequestParam 제약조건 실패
     * ========================= */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<BaseResponse<Void>> handleConstraintViolation(
            ConstraintViolationException ex
    ) {
        log.warn("ConstraintViolationException", ex);

        return ResponseEntity
                .badRequest()
                .body(BaseResponse.error(CommonErrorCode.INVALID_INPUT_VALUE));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseResponse<Void>> handleUnexpectedException(Exception ex) {
        log.error("Unhandled exception", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(BaseResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR));
    }
    public record ErrorResponse(String code, String message, String exception) {}

}
