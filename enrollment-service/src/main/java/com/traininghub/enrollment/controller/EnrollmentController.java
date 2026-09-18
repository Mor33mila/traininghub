package com.traininghub.enrollment.controller;

import com.traininghub.enrollment.dto.EnrollmentRequest;
import com.traininghub.enrollment.dto.EnrollmentResponse;
import com.traininghub.enrollment.dto.EnrollmentStatusRequest;
import com.traininghub.enrollment.entity.EnrollmentStatus;
import com.traininghub.enrollment.mapper.EnrollmentMapper;
import com.traininghub.enrollment.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/enrollments")
public class EnrollmentController {
    private final EnrollmentService service;
    private final EnrollmentMapper mapper;

    /** Crea l'adapter REST delle iscrizioni con servizio e mapper. */
    public EnrollmentController(EnrollmentService service, EnrollmentMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    /** Gestisce la creazione di un'iscrizione e restituisce HTTP 201. */
    public ResponseEntity<EnrollmentResponse> create(@Valid @RequestBody EnrollmentRequest request) {
        return ResponseEntity.status(201).body(mapper.toResponse(service.create(request)));
    }

    @GetMapping("/{id}")
    /** Gestisce la lettura del dettaglio di un'iscrizione. */
    public EnrollmentResponse getById(@PathVariable UUID id) {
        return mapper.toResponse(service.getById(id));
    }

    @GetMapping("/course/{courseId}")
    /** Elenca le iscrizioni di un corso con paginazione limitata. */
    public org.springframework.data.domain.Page<EnrollmentResponse> byCourse(
            @PathVariable UUID courseId, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.findByCourse(courseId, pageable(page, size)).map(mapper::toResponse);
    }

    @GetMapping("/participant/{participantId}")
    /** Elenca le iscrizioni di un partecipante con paginazione limitata. */
    public org.springframework.data.domain.Page<EnrollmentResponse> byParticipant(
            @PathVariable UUID participantId, @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.findByParticipant(participantId, pageable(page, size)).map(mapper::toResponse);
    }

    @PatchMapping("/{id}/status")
    /** Gestisce l'aggiornamento parziale dello stato previsto dall'API. */
    public EnrollmentResponse updateStatus(@PathVariable UUID id,
                                            @Valid @RequestBody EnrollmentStatusRequest request) {
        return mapper.toResponse(service.updateStatus(id, request.status()));
    }

    /** Costruisce una paginazione sicura e impedisce richieste troppo grandi. */
    private Pageable pageable(int page, int size) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by("enrollmentDate").descending());
    }
}