package com.traininghub.identity.service;

import com.traininghub.identity.dto.AuditEventRequest;
import com.traininghub.identity.dto.AuditEventResponse;
import com.traininghub.identity.entity.AuditAction;
import com.traininghub.identity.entity.AuditEvent;
import com.traininghub.identity.entity.AuditResource;
import com.traininghub.identity.repository.AuditEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class AuditService {
    private final AuditEventRepository repository;

    public AuditService(AuditEventRepository repository) {
        this.repository = repository;
    }

    public AuditEventResponse record(String actor, AuditEventRequest request) {
        AuditEvent event = new AuditEvent(actor, request.action(), request.resource(), request.recordId(), Instant.now());
        return toResponse(repository.save(event));
    }

    public Page<AuditEventResponse> search(int page, int size, String query, AuditResource resource,
                                           AuditAction action, Instant fromInclusive, Instant toExclusive) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String normalizedQuery = query == null || query.isBlank() ? null : query.trim();
        return repository.search(normalizedQuery, resource, action, fromInclusive, toExclusive,
                        PageRequest.of(safePage, safeSize))
                .map(AuditService::toResponse);
    }

    private static AuditEventResponse toResponse(AuditEvent event) {
        return new AuditEventResponse(event.getId(), event.getActor(), event.getAction(), event.getResource(),
                event.getRecordId(), event.getOccurredAt());
    }
}