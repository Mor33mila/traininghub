package com.traininghub.participant.mapper;

import com.traininghub.participant.dto.ParticipantRequest;
import com.traininghub.participant.dto.ParticipantResponse;
import com.traininghub.participant.entity.Participant;
import org.springframework.stereotype.Component;

@Component
public class ParticipantMapper {
    /** Crea un'entity partecipante dal DTO di richiesta validato. */
    public Participant toEntity(ParticipantRequest request) {
        Participant participant = new Participant();
        updateEntity(participant, request);
        return participant;
    }

    /** Copia i valori e normalizza codice fiscale ed e-mail per i confronti. */
    public void updateEntity(Participant participant, ParticipantRequest request) {
        participant.setFirstName(request.getFirstName().trim());
        participant.setLastName(request.getLastName().trim());
        participant.setTaxCode(request.getTaxCode().trim().toUpperCase());
        participant.setBirthDate(request.getBirthDate());
        participant.setEmail(request.getEmail().trim().toLowerCase());
        participant.setPhone(request.getPhone());
        participant.setEducationLevel(request.getEducationLevel());
        participant.setEmploymentStatus(request.getEmploymentStatus());
        if (participant.getActive() == null) participant.setActive(true);
    }

    /** Converte l'entity nel DTO pubblico di risposta del partecipante. */
    public ParticipantResponse toResponse(Participant participant) {
        return new ParticipantResponse(participant.getId(), participant.getFirstName(), participant.getLastName(),
                participant.getTaxCode(), participant.getBirthDate(), participant.getEmail(), participant.getPhone(),
                participant.getEducationLevel(), participant.getEmploymentStatus(), participant.getActive());
    }
}