package com.park.parkpro.exception;

import com.park.parkpro.dto.ErrorResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFoundException(NotFoundException ex) {
        ErrorResponseDto error = new ErrorResponseDto(ex.getStatusCode(), "Not Found", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponseDto> handleUnauthorizedException(UnauthorizedException ex) {
        ErrorResponseDto error = new ErrorResponseDto(ex.getStatusCode(), "Unauthorized", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponseDto> handleForbiddenException(ForbiddenException ex) {
        ErrorResponseDto error = new ErrorResponseDto(ex.getStatusCode(), "Forbidden", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequestException(BadRequestException ex) {
        ErrorResponseDto error = new ErrorResponseDto(ex.getStatusCode(), "Bad Request", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDto> handleConflictException(ConflictException ex) {
        ErrorResponseDto error = new ErrorResponseDto(ex.getStatusCode(), "Conflict", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(HttpStatus.FORBIDDEN.value(), "Forbidden", "You do not have permission to access this resource");
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGenericException(Exception ex, WebRequest request) {
        ErrorResponseDto error = new ErrorResponseDto(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", "An unexpected error occurred: " + ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
