package com.traininghub.participant.service;

import com.traininghub.participant.dto.ParticipantRequest;
import com.traininghub.participant.entity.Participant;
import com.traininghub.participant.exception.DuplicateParticipantException;
import com.traininghub.participant.exception.ParticipantNotFoundException;
import com.traininghub.participant.mapper.ParticipantMapper;
import com.traininghub.participant.repository.ParticipantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
public class ParticipantService {
    private final ParticipantRepository repository;
    private final ParticipantMapper mapper;

    /** Crea il servizio partecipanti con repository e mapper dei DTO. */
    public ParticipantService(ParticipantRepository repository, ParticipantMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional
    /** Normalizza, verifica l'unicita' e salva un nuovo partecipante. */
    public Participant create(ParticipantRequest request) {
        String taxCode = request.getTaxCode().trim().toUpperCase();
        String email = request.getEmail().trim().toLowerCase();
        if (repository.existsByTaxCodeIgnoreCase(taxCode)) throw new DuplicateParticipantException("tax code");
        if (repository.existsByEmailIgnoreCase(email)) throw new DuplicateParticipantException("email");
        return repository.save(mapper.toEntity(request));
    }

    @Transactional(readOnly = true)
    /** Carica un partecipante oppure solleva l'eccezione di risorsa non trovata. */
    public Participant getById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new ParticipantNotFoundException(id));
    }

    @Transactional(readOnly = true)
    /** Cerca per cognome, codice fiscale o e-mail con supporto alla paginazione. */
    public Page<Participant> search(String query, Pageable pageable) {
        if (query == null || query.isBlank()) return repository.findAll(pageable);
        String value = query.trim();
        return repository.findByLastNameContainingIgnoreCaseOrTaxCodeContainingIgnoreCaseOrEmailContainingIgnoreCase(
                value, value, value, pageable);
    }

    @Transactional
    /** Aggiorna un partecipante mantenendo i vincoli di unicita'. */
    public Participant update(UUID id, ParticipantRequest request) {
        Participant participant = getById(id);
        String taxCode = request.getTaxCode().trim().toUpperCase();
        String email = request.getEmail().trim().toLowerCase();
        if (repository.existsByTaxCodeIgnoreCaseAndIdNot(taxCode, id))
            throw new DuplicateParticipantException("tax code");
        if (repository.existsByEmailIgnoreCaseAndIdNot(email, id))
            throw new DuplicateParticipantException("email");
        mapper.updateEntity(participant, request);
        return repository.save(participant);
    }

    @Transactional
    /** Disattiva logicamente il partecipante senza cancellarne lo storico. */
    public void deactivate(UUID id) {
        Participant participant = getById(id);
        participant.setActive(false);
        repository.save(participant);
    }
}