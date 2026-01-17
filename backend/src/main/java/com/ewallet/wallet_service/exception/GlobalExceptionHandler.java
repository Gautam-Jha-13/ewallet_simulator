package com.ewallet.wallet_service.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =============================
    // 404 - RESOURCE NOT FOUND
    // =============================
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    // =============================
    // 400 - INVALID REQUEST
    // =============================
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiErrorResponse> handleBadRequest(
            InvalidRequestException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    // =============================
    // 400 - INSUFFICIENT BALANCE
    // =============================
    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientBalance(
            InsufficientBalanceException ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    // =============================
    // 400 - DTO VALIDATION ERRORS
    // =============================
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex
    ) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult()
          .getFieldErrors()
          .forEach(error ->
                  errors.put(
                          error.getField(),
                          error.getDefaultMessage()
                  )
          );

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errors);
    }

    // =============================
    // 500 - GENERIC EXCEPTION
    // =============================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleGeneric(
            Exception ex,
            HttpServletRequest request
    ) {
        return buildResponse(ex, HttpStatus.INTERNAL_SERVER_ERROR, request);
    }

    // =============================
    // COMMON RESPONSE BUILDER
    // =============================
    private ResponseEntity<ApiErrorResponse> buildResponse(
            Exception ex,
            HttpStatus status,
            HttpServletRequest request
    ) {
        ApiErrorResponse error = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                ex.getMessage(),
                request.getRequestURI()
        );

        return ResponseEntity
                .status(status)
                .body(error);
    }
}
