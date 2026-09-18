package com.traininghub.enrollment.mapper;

import com.traininghub.enrollment.dto.EnrollmentResponse;
import com.traininghub.enrollment.entity.Enrollment;
import org.springframework.stereotype.Component;

@Component
public class EnrollmentMapper {
    /** Converte un'entity iscrizione nel DTO di risposta REST. */
    public EnrollmentResponse toResponse(Enrollment enrollment) {
        return new EnrollmentResponse(enrollment.getId(), enrollment.getCourseId(), enrollment.getParticipantId(),
                enrollment.getEnrollmentDate(), enrollment.getStatus());
    }
}