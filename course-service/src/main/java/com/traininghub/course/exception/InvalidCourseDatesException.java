package com.traininghub.course.exception;

public class InvalidCourseDatesException extends RuntimeException {
    public InvalidCourseDatesException() {
        super("The end date must be equal to or later than the start date");
    }
}