package com.traininghub.enrollment.dto;

import com.traininghub.enrollment.entity.EnrollmentStatus;
import java.time.LocalDate;
import java.util.UUID;

public record EnrollmentResponse(UUID id, UUID courseId, UUID participantId,
                                 LocalDate enrollmentDate, EnrollmentStatus status) { }