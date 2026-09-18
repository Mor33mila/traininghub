package com.traininghub.participant.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ParticipantResponse(UUID id, String firstName, String lastName, String taxCode,
                                  LocalDate birthDate, String email, String phone,
                                  String educationLevel, String employmentStatus, Boolean active) {
}