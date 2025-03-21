package com.qairline.qairline_backend.common.exception.handler;

import com.qairline.qairline_backend.common.api.ApiResponse;
import com.qairline.qairline_backend.common.exception.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice

public class ExceptionHandlerConfig {
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ApiResponse> apiException(BusinessException e) {
        int code = e.getStatus() != null ? e.getStatus().value() : 777;
        if (e.getMessage() != null) {
            return ResponseEntity.ok(new ApiResponse(code, e.getMessage(), e.getMessage()));
        }
        return ResponseEntity.ok(new ApiResponse(code, "An error has occurred"));

    }
}

