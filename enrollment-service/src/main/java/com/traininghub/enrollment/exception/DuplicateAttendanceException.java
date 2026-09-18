package com.traininghub.enrollment.exception;

public class DuplicateAttendanceException extends RuntimeException {
    public DuplicateAttendanceException() { super("Attendance already exists for this enrollment and lesson date"); }
}