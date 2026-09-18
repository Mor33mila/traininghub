package com.traininghub.enrollment.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public record AttendanceResponse(UUID id, UUID enrollmentId, LocalDate lessonDate,
                                 LocalTime entryTime, LocalTime exitTime,
                                 BigDecimal attendedHours, Boolean absent, String justification) { }