package com.traininghub.identity.controller;

import com.traininghub.identity.dto.AuditEventRequest;
import com.traininghub.identity.dto.AuditEventResponse;
import com.traininghub.identity.entity.AuditAction;
import com.traininghub.identity.entity.AuditResource;
import com.traininghub.identity.service.AuditService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;
import java.util.Objects;

@RestController
@RequestMapping("/api/audit/events")
public class AuditController {
    private final AuditService service;

    public AuditController(AuditService service) {
        this.service = service;
    }

    @PostMapping
    public AuditEventResponse record(@Valid @RequestBody AuditEventRequest request,
                                     @AuthenticationPrincipal Jwt jwt) {
        String actor = Objects.requireNonNullElse(jwt.getClaimAsString("username"), jwt.getSubject());
        return service.record(actor, request);
    }

    @GetMapping
    public Page<AuditEventResponse> search(@RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size,
                                           @RequestParam(required = false) String query,
                                           @RequestParam(required = false) AuditResource resource,
                                           @RequestParam(required = false) AuditAction action,
                                           @RequestParam(required = false) Instant from,
                                           @RequestParam(required = false) Instant to) {
        return service.search(page, size, query, resource, action, from, to);
    }
}