package com.traininghub.enrollment.controller;

import com.traininghub.enrollment.dto.AttendanceRequest;
import com.traininghub.enrollment.dto.AttendanceResponse;
import com.traininghub.enrollment.dto.ParticipantFrequencyResponse;
import com.traininghub.enrollment.mapper.AttendanceMapper;
import com.traininghub.enrollment.service.AttendanceService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    private final AttendanceService service;
    private final AttendanceMapper mapper;

    /** Crea l'adapter REST delle presenze con servizio e mapper. */
    public AttendanceController(AttendanceService service, AttendanceMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    @PostMapping
    /** Gestisce la registrazione di una presenza e restituisce HTTP 201. */
    public ResponseEntity<AttendanceResponse> create(@Valid @RequestBody AttendanceRequest request) {
        return ResponseEntity.status(201).body(mapper.toResponse(service.create(request)));
    }

    @GetMapping("/course/{courseId}")
    /** Elenca tutte le presenze associate a un corso. */
    public List<AttendanceResponse> byCourse(@PathVariable UUID courseId,
                                             @AuthenticationPrincipal Jwt jwt) {
        UUID instructorId = isTeacher(jwt) ? UUID.fromString(jwt.getSubject()) : null;
        return service.findByCourse(courseId, instructorId).stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/participant/{participantId}/percentage")
    /** Restituisce la percentuale aggregata di frequenza di un partecipante. */
    public ParticipantFrequencyResponse percentage(@PathVariable UUID participantId) {
        return service.calculateParticipantFrequency(participantId);
    }

    private boolean isTeacher(Jwt jwt) {
        return jwt != null && jwt.getClaimAsStringList("roles") != null
                && jwt.getClaimAsStringList("roles").contains("TEACHER");
    }
}