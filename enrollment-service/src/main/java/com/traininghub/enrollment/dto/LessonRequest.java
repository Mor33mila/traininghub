package com.traininghub.enrollment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record LessonRequest(
        @NotNull UUID courseId,
        @NotBlank @Size(max = 120) String title,
        @NotNull LocalDate lessonDate,
        @NotNull LocalTime startTime,
        @NotNull LocalTime endTime,
        @Size(max = 500) String notes) { }