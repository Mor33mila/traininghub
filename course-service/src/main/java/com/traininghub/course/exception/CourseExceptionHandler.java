package com.traininghub.course.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class CourseExceptionHandler {
    @ExceptionHandler(CourseNotFoundException.class)
    /** Converte i corsi mancanti in una risposta HTTP 404 coerente. */
    public ResponseEntity<ApiError> handleNotFound(CourseNotFoundException exception,
                                                    HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler({DuplicateCourseCodeException.class, DataIntegrityViolationException.class})
    /** Converte codici duplicati e violazioni di unicita' del database in HTTP 409. */
    public ResponseEntity<ApiError> handleConflict(RuntimeException exception, HttpServletRequest request) {
        String message = exception instanceof DuplicateCourseCodeException
                ? exception.getMessage() : "Course code already exists";
        return build(HttpStatus.CONFLICT, message, request, Map.of());
    }

    @ExceptionHandler(InvalidCourseDatesException.class)
    /** Converte intervalli di date corso non validi in HTTP 400. */
    public ResponseEntity<ApiError> handleInvalidDates(InvalidCourseDatesException exception,
                                                        HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request,
                Map.of("endDate", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    /** Converte gli errori Bean Validation in dettagli di campo HTTP 400. */
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception,
                                                      HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Request validation failed", request, errors);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    /** Converte valori enum non validi nel path o nella query in HTTP 400. */
    public ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException exception,
                                                        HttpServletRequest request) {
        String message = "Invalid value for parameter: " + exception.getName();
        return build(HttpStatus.BAD_REQUEST, message, request, Map.of(exception.getName(), message));
    }

    /** Crea la struttura di errore comune usata da tutti gli handler. */
    private ResponseEntity<ApiError> build(HttpStatus status, String message,
                                           HttpServletRequest request, Map<String, String> errors) {
        return ResponseEntity.status(status).body(new ApiError(
                OffsetDateTime.now(), status.value(), message, request.getRequestURI(), errors));
    }
}