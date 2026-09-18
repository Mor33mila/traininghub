package com.traininghub.participant.exception;

public class DuplicateParticipantException extends RuntimeException {
    public DuplicateParticipantException(String field) { super("Participant already exists with " + field); }
}