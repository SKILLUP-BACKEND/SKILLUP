package com.example.skillup.global.exception.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ValidationErrors {
    private List<FieldErrorDetail> errors;
    private String errorType;
    @Getter
    @AllArgsConstructor
    public static class FieldErrorDetail {
        private String field;
        private String message;
    }
}
