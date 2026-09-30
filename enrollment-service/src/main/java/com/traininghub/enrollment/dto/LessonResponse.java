package com.traininghub.enrollment.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record LessonResponse(UUID id, UUID courseId, String title, LocalDate lessonDate,
                             LocalTime startTime, LocalTime endTime, String notes) { }