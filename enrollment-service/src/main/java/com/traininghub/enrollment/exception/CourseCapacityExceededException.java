package com.traininghub.enrollment.exception;

public class CourseCapacityExceededException extends RuntimeException {
    public CourseCapacityExceededException() { super("Course maximum capacity has been reached"); }
}