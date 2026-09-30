package com.traininghub.identity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_events", indexes = {
        @Index(name = "idx_audit_occurred_at", columnList = "occurred_at"),
        @Index(name = "idx_audit_actor", columnList = "actor")
})
public class AuditEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 80)
    private String actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private AuditAction action;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private AuditResource resource;

    @Column(name = "record_id", length = 80)
    private String recordId;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;

    protected AuditEvent() { }

    public AuditEvent(String actor, AuditAction action, AuditResource resource, String recordId, Instant occurredAt) {
        this.actor = actor;
        this.action = action;
        this.resource = resource;
        this.recordId = recordId;
        this.occurredAt = occurredAt;
    }

    public UUID getId() { return id; }
    public String getActor() { return actor; }
    public AuditAction getAction() { return action; }
    public AuditResource getResource() { return resource; }
    public String getRecordId() { return recordId; }
    public Instant getOccurredAt() { return occurredAt; }
}