package com.traininghub.enrollment.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class EnrollmentRequest {
    @NotNull
    private UUID courseId;
    @NotNull
    private UUID participantId;

    public UUID getCourseId() { return courseId; }
    public void setCourseId(UUID courseId) { this.courseId = courseId; }
    public UUID getParticipantId() { return participantId; }
    public void setParticipantId(UUID participantId) { this.participantId = participantId; }
}