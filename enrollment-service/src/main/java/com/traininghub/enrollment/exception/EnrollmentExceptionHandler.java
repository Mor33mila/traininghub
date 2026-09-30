package com.traininghub.enrollment.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class EnrollmentExceptionHandler {
    @ExceptionHandler(EnrollmentNotFoundException.class)
    /** Converte un'iscrizione mancante in HTTP 404. */
    public ResponseEntity<ApiError> notFound(EnrollmentNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(LessonNotFoundException.class)
    public ResponseEntity<ApiError> lessonNotFound(LessonNotFoundException exception, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler({DuplicateEnrollmentException.class, CourseCapacityExceededException.class,
            DuplicateAttendanceException.class, DataIntegrityViolationException.class})
    /** Converte duplicati, corsi pieni e presenze duplicate in HTTP 409. */
    public ResponseEntity<ApiError> conflict(RuntimeException exception, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(EnrollmentDependencyException.class)
    /** Converte dati remoti non disponibili o non validi in HTTP 502. */
    public ResponseEntity<ApiError> dependency(EnrollmentDependencyException exception,
                                                HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(InvalidAttendanceException.class)
    /** Converte le violazioni delle regole presenze in HTTP 400. */
    public ResponseEntity<ApiError> invalidAttendance(InvalidAttendanceException exception,
                                                       HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(InvalidLessonException.class)
    public ResponseEntity<ApiError> invalidLesson(InvalidLessonException exception, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request, Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    /** Converte gli errori di validazione richiesta in errori di campo HTTP 400. */
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception,
                                                HttpServletRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return build(HttpStatus.BAD_REQUEST, "Request validation failed", request, errors);
    }

    /** Costruisce la risposta di errore comune alle iscrizioni. */
    private ResponseEntity<ApiError> build(HttpStatus status, String message, HttpServletRequest request,
                                           Map<String, String> errors) {
        return ResponseEntity.status(status).body(new ApiError(OffsetDateTime.now(), status.value(),
                message, request.getRequestURI(), errors));
    }
}