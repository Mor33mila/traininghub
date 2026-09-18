package com.traininghub.participant.controller;

import com.traininghub.participant.dto.ParticipantRequest;
import com.traininghub.participant.dto.ParticipantResponse;
import com.traininghub.participant.mapper.ParticipantMapper;
import com.traininghub.participant.service.ParticipantService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/participants")
public class ParticipantController {
    private final ParticipantService service;
    private final ParticipantMapper mapper;

    /** Crea l'adapter REST con servizio partecipanti e mapper. */
    public ParticipantController(ParticipantService service, ParticipantMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    /** Gestisce la creazione di un partecipante e restituisce HTTP 201. */
    public ResponseEntity<ParticipantResponse> create(@Valid @RequestBody ParticipantRequest request) {
        ParticipantResponse response = mapper.toResponse(service.create(request));
        return ResponseEntity.status(201).body(response);
    }

    @GetMapping
    /** Gestisce la ricerca paginata dei partecipanti. */
    public Page<ParticipantResponse> search(@RequestParam(required = false) String query,
                                            @RequestParam(defaultValue = "0") int page,
                                            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by("lastName").ascending());
        return service.search(query, pageable).map(mapper::toResponse);
    }

    @GetMapping("/{id}")
    /** Gestisce la lettura del dettaglio di un partecipante. */
    public ParticipantResponse getById(@PathVariable UUID id) {
        return mapper.toResponse(service.getById(id));
    }

    @PutMapping("/{id}")
    /** Gestisce l'aggiornamento completo di un partecipante. */
    public ParticipantResponse update(@PathVariable UUID id, @Valid @RequestBody ParticipantRequest request) {
        return mapper.toResponse(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    /** Gestisce la disattivazione logica e restituisce HTTP 204. */
    public ResponseEntity<Void> deactivate(@PathVariable UUID id) {
        service.deactivate(id);
        return ResponseEntity.noContent().build();
    }
}