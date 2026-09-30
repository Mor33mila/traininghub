package com.traininghub.identity.service;

import com.traininghub.identity.dto.AuditEventRequest;
import com.traininghub.identity.dto.AuditEventResponse;
import com.traininghub.identity.entity.AuditAction;
import com.traininghub.identity.entity.AuditEvent;
import com.traininghub.identity.entity.AuditResource;
import com.traininghub.identity.repository.AuditEventRepository;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuditServiceTest {
    private final AuditEventRepository repository = mock(AuditEventRepository.class);
    private final AuditService service = new AuditService(repository);

    @Test
    void recordsActorAndChangeMetadataWithoutPayload() {
        when(repository.save(any(AuditEvent.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AuditEventResponse event = service.record("mario.rossi",
                new AuditEventRequest(AuditAction.UPDATE, AuditResource.COURSE, "course-123"));

        assertEquals("mario.rossi", event.actor());
        assertEquals(AuditAction.UPDATE, event.action());
        assertEquals(AuditResource.COURSE, event.resource());
        assertEquals("course-123", event.recordId());
        assertNotNull(event.occurredAt());
    }
}