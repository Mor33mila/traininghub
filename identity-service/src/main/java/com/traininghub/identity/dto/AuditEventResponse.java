package com.traininghub.identity.dto;

import com.traininghub.identity.entity.AuditAction;
import com.traininghub.identity.entity.AuditResource;
import java.time.Instant;
import java.util.UUID;

public record AuditEventResponse(
        UUID id,
        String actor,
        AuditAction action,
        AuditResource resource,
        String recordId,
        Instant occurredAt) { }