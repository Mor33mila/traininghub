package com.traininghub.enrollment.exception;

import java.util.UUID;

public class EnrollmentNotFoundException extends RuntimeException {
    public EnrollmentNotFoundException(UUID id) { super("Enrollment not found: " + id); }
}