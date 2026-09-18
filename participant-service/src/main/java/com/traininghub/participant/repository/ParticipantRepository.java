package com.traininghub.participant.repository;

import com.traininghub.participant.entity.Participant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ParticipantRepository extends JpaRepository<Participant, UUID> {
    boolean existsByTaxCodeIgnoreCase(String taxCode);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByTaxCodeIgnoreCaseAndIdNot(String taxCode, UUID id);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
    Page<Participant> findByLastNameContainingIgnoreCaseOrTaxCodeContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String lastName, String taxCode, String email, Pageable pageable);
}