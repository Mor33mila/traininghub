package com.traininghub.participant.exception;

import java.util.UUID;

public class ParticipantNotFoundException extends RuntimeException {
    public ParticipantNotFoundException(UUID id) { super("Participant not found: " + id); }
}