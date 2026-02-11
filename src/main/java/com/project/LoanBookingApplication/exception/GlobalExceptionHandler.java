package com.project.LoanBookingApplication.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // DTO validation errors (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationErrors(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getFieldErrors()
                .forEach(error ->
                        errors.put(error.getField(), error.getDefaultMessage())
                );

        return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<Map<String, Object>> handleConflict(ConflictException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(BadRequestException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(JsonProcessingException.class)
    public ResponseEntity<Map<String, Object>> handleJsonProcessing(JsonProcessingException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", "Invalid JSON: " + (ex.getOriginalMessage() != null ? ex.getOriginalMessage() : ex.getMessage()));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgument(IllegalArgumentException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage() != null ? ex.getMessage() : "Invalid parameter value");
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<?> handleEnumErrors(HttpMessageNotReadableException ex) {

        Throwable cause = ex.getCause();

        if (cause instanceof InvalidFormatException ife) {

            Class<?> targetType = ife.getTargetType();

            if (targetType.isEnum()) {

                String fieldName = ife.getPath().get(0).getFieldName();
                Object invalidValue = ife.getValue();

                String allowedValues = Arrays.stream(targetType.getEnumConstants())
                        .map(Object::toString)
                        .collect(Collectors.joining(", "));

                return ResponseEntity.badRequest().body(
                        Map.of(
                                "field", fieldName,
                                "invalidValue", invalidValue,
                                "message", "Invalid value for enum",
                                "allowedValues", allowedValues
                        )
                );
            }
        }

        return ResponseEntity.badRequest().body(
                Map.of("error", "Invalid request payload")
        );
    }
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handleNoHandlerFound(NoHandlerFoundException ex) {

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                Map.of(
                        "error", "Invalid API endpoint",
                        "method", ex.getHttpMethod(),
                        "path", ex.getRequestURL(),
                        "message", "The requested API does not exist"
                )
        );
    }
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.NOT_FOUND.value());
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // @ExceptionHandler(DataIntegrityViolationException.class)
    // public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
    //     Map<String, Object> body = new LinkedHashMap<>();
    //     body.put("timestamp", LocalDateTime.now());
    //     body.put("status", HttpStatus.CONFLICT.value());
    //     body.put("error", "Conflict");
    //     String message = "Data integrity violation";
    //     if (ex.getCause() != null && ex.getCause().getMessage() != null) {
    //         String causeMsg = ex.getCause().getMessage();
    //         if (causeMsg.contains("unique") || causeMsg.contains("Unique") || causeMsg.contains("duplicate")) {
    //             message = "Resource already exists with the same unique value.";
    //         } else if (causeMsg.contains("foreign key") || causeMsg.contains("constraint")) {
    //             message = "Referenced resource does not exist or constraint violated.";
    //         } else {
    //             message = causeMsg.length() > 200 ? causeMsg.substring(0, 200) + "..." : causeMsg;
    //         }
    //     }
    //     body.put("message", message);
    //     return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    // }

    @ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<Map<String, Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {

    Map<String, Object> body = new LinkedHashMap<>();

    body.put("timestamp", LocalDateTime.now());
    body.put("status", HttpStatus.CONFLICT.value());
    body.put("error", "Conflict");

    String message = "Data integrity violation";

    Throwable root = ex.getMostSpecificCause();

    if (root != null && root.getMessage() != null) {

        String causeMsg = root.getMessage().toLowerCase();

        if (causeMsg.contains("duplicate") || causeMsg.contains("unique")) {
            message = "Resource already exists with the same unique value.";
        }
        else if (causeMsg.contains("foreign key")) {
            message = "Referenced resource does not exist.";
        }
        else if (causeMsg.contains("not-null") || causeMsg.contains("null")) {
            message = "Required field cannot be null.";
        }
        else {
            message = causeMsg.length() > 200
                    ? causeMsg.substring(0, 200) + "..."
                    : causeMsg;
        }
    }

    body.put("message", message);

    return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
}


    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getConstraintViolations().forEach(v ->
                errors.put(v.getPropertyPath().toString(), v.getMessage()));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Failed");
        body.put("details", errors);
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalState(IllegalStateException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }


}
