package com.park.parkpro.dto;

import java.time.LocalDateTime;

public class ErrorResponseDto {
    private final int status;
    private final String error;
    private final String message;
    private final LocalDateTime timestamp;

    public ErrorResponseDto(int status, String error, String message) {
        this.status = status;
        this.error = error;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}