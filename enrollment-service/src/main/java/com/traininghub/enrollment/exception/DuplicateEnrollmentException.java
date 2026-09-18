package com.traininghub.enrollment.exception;

public class DuplicateEnrollmentException extends RuntimeException {
    public DuplicateEnrollmentException() { super("Participant is already actively enrolled in this course"); }
}