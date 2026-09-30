package com.traininghub.identity.repository;

import com.traininghub.identity.entity.AuditAction;
import com.traininghub.identity.entity.AuditEvent;
import com.traininghub.identity.entity.AuditResource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import java.time.Instant;
import static org.junit.jupiter.api.Assertions.assertEquals;

@DataJpaTest
class AuditEventRepositoryTest {
    @Autowired
    private AuditEventRepository repository;

    @Test
    void searchesAcrossActorAndRecordIdAndFiltersByResource() {
        repository.save(new AuditEvent("mario.rossi", AuditAction.CREATE, AuditResource.COURSE,
                "course-1", Instant.parse("2026-09-30T10:00:00Z")));
        repository.save(new AuditEvent("tutor", AuditAction.UPDATE, AuditResource.PARTICIPANT,
                "participant-2", Instant.parse("2026-09-30T10:01:00Z")));
        repository.save(new AuditEvent("mario.rossi", AuditAction.UPDATE, AuditResource.COURSE,
                "course-3", Instant.parse("2026-09-30T10:02:00Z")));

        Page<AuditEvent> byActor = repository.search("mario", null, null, null, null, PageRequest.of(0, 1));
        Page<AuditEvent> byResource = repository.search(null, AuditResource.COURSE, null, null, null,
                PageRequest.of(0, 10));
        Page<AuditEvent> byActionAndDate = repository.search(null, null, AuditAction.UPDATE,
                Instant.parse("2026-09-30T10:02:00Z"), Instant.parse("2026-09-30T10:03:00Z"),
                PageRequest.of(0, 10));

        assertEquals(2, byActor.getTotalElements());
        assertEquals("course-3", byActor.getContent().getFirst().getRecordId());
        assertEquals(2, byResource.getTotalElements());
        assertEquals(1, byActionAndDate.getTotalElements());
        assertEquals("course-3", byActionAndDate.getContent().getFirst().getRecordId());
    }
}