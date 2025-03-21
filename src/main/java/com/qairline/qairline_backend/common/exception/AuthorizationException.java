package com.qairline.qairline_backend.common.exception;

import org.springframework.http.HttpStatus;

public class AuthorizationException extends BusinessException{
    public AuthorizationException(HttpStatus status, String message) {
        super(status, message);
    }

    public AuthorizationException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
