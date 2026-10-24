package com.taskflow.api.config;

import com.taskflow.api.dto.ApiModels.ErrorResponse;
import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import java.time.Instant;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErrorResponse> validation(MethodArgumentNotValidException ex){String message=ex.getBindingResult().getFieldErrors().stream().map(FieldError::getDefaultMessage).collect(Collectors.joining("; "));return ResponseEntity.badRequest().body(new ErrorResponse("validation_error",message,Instant.now()));}
    @ExceptionHandler(ResponseStatusException.class)
    ResponseEntity<ErrorResponse> status(ResponseStatusException ex){return ResponseEntity.status(ex.getStatusCode()).body(new ErrorResponse("request_error",ex.getReason(),Instant.now()));}
    @ExceptionHandler(IllegalArgumentException.class)
    ResponseEntity<ErrorResponse> badRequest(IllegalArgumentException ex){return ResponseEntity.badRequest().body(new ErrorResponse("request_error",ex.getMessage(),Instant.now()));}
}
