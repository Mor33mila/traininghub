package com.traininghub.enrollment.exception;

import java.util.UUID;

public class LessonNotFoundException extends RuntimeException {
    public LessonNotFoundException(UUID id) {
        super("Lesson not found: " + id);
    }
}