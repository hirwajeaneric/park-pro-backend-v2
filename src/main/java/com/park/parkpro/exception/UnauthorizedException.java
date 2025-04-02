package com.park.parkpro.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends CustomException {
    public UnauthorizedException(String message) {
        super(message, HttpStatus.UNAUTHORIZED.value()); // 401
    }
}
