package com.traininghub.course.exception;

public class DuplicateCourseCodeException extends RuntimeException {
    public DuplicateCourseCodeException(String courseCode) {
        super("Course code already exists: " + courseCode);
    }
}