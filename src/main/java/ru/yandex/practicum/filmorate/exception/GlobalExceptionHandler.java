package ru.yandex.practicum.filmorate.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String ERROR_KEY = "error";

    private static final String VALIDATION_ERROR_MESSAGE = "Ошибка валидации";
    private static final String VALIDATION_DATA_ERROR_MESSAGE = "Ошибка валидации данных";
    private static final String INTERNAL_SERVER_ERROR_MESSAGE = "Внутренняя ошибка сервера";

    private static final int FIRST_ELEMENT_INDEX = 0;

    private static final HttpStatus BAD_REQUEST_STATUS = HttpStatus.BAD_REQUEST;
    private static final HttpStatus NOT_FOUND_STATUS = HttpStatus.NOT_FOUND;
    private static final HttpStatus INTERNAL_SERVER_ERROR_STATUS = HttpStatus.INTERNAL_SERVER_ERROR;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = VALIDATION_ERROR_MESSAGE;

        if (ex.getBindingResult().getFieldErrors() != null
                && !ex.getBindingResult().getFieldErrors().isEmpty()) {
            errorMessage = ex.getBindingResult()
                    .getFieldErrors()
                    .get(FIRST_ELEMENT_INDEX)
                    .getDefaultMessage();
        }

        return ResponseEntity.status(BAD_REQUEST_STATUS)
                .body(Map.of(ERROR_KEY, errorMessage));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, String>> handleConstraintViolation(ConstraintViolationException ex) {
        return ResponseEntity.status(BAD_REQUEST_STATUS)
                .body(Map.of(ERROR_KEY, VALIDATION_DATA_ERROR_MESSAGE));
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<Map<String, String>> handleValidationException(ValidationException e) {
        return ResponseEntity.status(BAD_REQUEST_STATUS)
                .body(Map.of(ERROR_KEY, e.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleNotFoundException(IllegalArgumentException e) {
        return ResponseEntity.status(NOT_FOUND_STATUS)
                .body(Map.of(ERROR_KEY, e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleException(Exception e) {
        return ResponseEntity.status(INTERNAL_SERVER_ERROR_STATUS)
                .body(Map.of(ERROR_KEY, INTERNAL_SERVER_ERROR_MESSAGE));
    }
}