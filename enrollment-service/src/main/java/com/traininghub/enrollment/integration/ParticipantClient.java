package com.traininghub.enrollment.integration;

import java.util.UUID;

public interface ParticipantClient {
    ParticipantSummary findById(UUID participantId);
}