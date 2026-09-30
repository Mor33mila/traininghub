package com.traininghub.identity.dto;

import com.traininghub.identity.entity.AuditAction;
import com.traininghub.identity.entity.AuditResource;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AuditEventRequest(
        @NotNull AuditAction action,
        @NotNull AuditResource resource,
        @Size(max = 80) String recordId) { }