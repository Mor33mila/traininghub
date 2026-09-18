package com.traininghub.enrollment.repository;

import com.traininghub.enrollment.entity.Enrollment;
import com.traininghub.enrollment.entity.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;
import java.util.List;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {
    /** Verifica se il partecipante ha gia' un'iscrizione attiva al corso. */
    boolean existsByCourseIdAndParticipantIdAndStatusIn(UUID courseId, UUID participantId,
                                                        Iterable<EnrollmentStatus> statuses);
    /** Conta le iscrizioni attive usate dalla regola di capienza. */
    long countByCourseIdAndStatusIn(UUID courseId, Iterable<EnrollmentStatus> statuses);
    /** Restituisce le iscrizioni paginato di un corso. */
    Page<Enrollment> findByCourseId(UUID courseId, Pageable pageable);
    /** Restituisce le iscrizioni paginato di un partecipante. */
    Page<Enrollment> findByParticipantId(UUID participantId, Pageable pageable);
    /** Restituisce tutte le iscrizioni usate per la frequenza aggregata. */
    List<Enrollment> findAllByParticipantId(UUID participantId);
}