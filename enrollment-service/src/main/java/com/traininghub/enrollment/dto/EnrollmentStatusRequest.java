package com.traininghub.enrollment.dto;

import com.traininghub.enrollment.entity.EnrollmentStatus;
import jakarta.validation.constraints.NotNull;

public record EnrollmentStatusRequest(@NotNull EnrollmentStatus status) { }