package com.traininghub.course.dto;

import com.traininghub.course.entity.CourseMode;
import com.traininghub.course.entity.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record CourseResponse(UUID id, String courseCode, String title, String description,
                             String trainingArea, BigDecimal totalHours, LocalDate startDate,
                             LocalDate endDate, Integer maximumCapacity, CourseMode mode,
                             CourseStatus status, UUID instructorId) {
}