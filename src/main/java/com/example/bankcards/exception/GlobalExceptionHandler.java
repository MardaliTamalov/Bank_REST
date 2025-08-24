package com.example.bankcards.exception;

import io.swagger.v3.oas.annotations.Hidden;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Hidden
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CardNotFoundException.class)
    public ResponseEntity<Map<String, Object>>handleCardNotFoundException(CardNotFoundException ex, WebRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
       response.put("error", "Card Not Found");
       response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);

    }
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleEntityNotFoundException(
            EntityNotFoundException ex,
            WebRequest request) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "Entity Not Found");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<Map<String, Object>> handleInsufficientFundsException(
            InsufficientFundsException ex,
            WebRequest request) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Insufficient Funds");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(
            UserNotFoundException ex,
            WebRequest request) {

        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.NOT_FOUND.value());
        response.put("error", "User Not Found");
        response.put("message", ex.getMessage());
        response.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions( MethodArgumentNotValidException ex,WebRequest request) {

        // 1. Собираем ошибки по полям
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage())
        );

        // 2. Формируем полный ответ
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("path", request.getDescription(false).replace("uri=", ""));
        response.put("errors", fieldErrors);  // Основные ошибки теперь в поле "errors"

        // 3. Возвращаем с HTTP 400
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response);
    }
}



