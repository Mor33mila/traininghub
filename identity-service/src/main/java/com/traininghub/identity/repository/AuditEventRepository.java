package com.traininghub.identity.repository;

import com.traininghub.identity.entity.AuditEvent;
import com.traininghub.identity.entity.AuditAction;
import com.traininghub.identity.entity.AuditResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.Instant;
import java.util.UUID;

public interface AuditEventRepository extends JpaRepository<AuditEvent, UUID> {
    @Query("""
            select event from AuditEvent event
            where (:resource is null or event.resource = :resource)
                                                        and (:action is null or event.action = :action)
                                                        and (:fromInclusive is null or event.occurredAt >= :fromInclusive)
                                                        and (:toExclusive is null or event.occurredAt < :toExclusive)
              and (:query is null
                   or lower(event.actor) like lower(concat('%', :query, '%'))
                   or lower(coalesce(event.recordId, '')) like lower(concat('%', :query, '%')))
            order by event.occurredAt desc, event.id desc
            """)
    Page<AuditEvent> search(@Param("query") String query,
                            @Param("resource") AuditResource resource,
                            @Param("action") AuditAction action,
                            @Param("fromInclusive") Instant fromInclusive,
                            @Param("toExclusive") Instant toExclusive,
                            Pageable pageable);
}