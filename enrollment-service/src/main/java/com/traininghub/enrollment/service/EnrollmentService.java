package com.traininghub.enrollment.service;

import com.traininghub.enrollment.dto.EnrollmentRequest;
import com.traininghub.enrollment.entity.Enrollment;
import com.traininghub.enrollment.entity.EnrollmentStatus;
import com.traininghub.enrollment.exception.CourseCapacityExceededException;
import com.traininghub.enrollment.exception.DuplicateEnrollmentException;
import com.traininghub.enrollment.exception.EnrollmentDependencyException;
import com.traininghub.enrollment.exception.EnrollmentNotFoundException;
import com.traininghub.enrollment.integration.CourseClient;
import com.traininghub.enrollment.integration.CourseSummary;
import com.traininghub.enrollment.integration.ParticipantClient;
import com.traininghub.enrollment.integration.ParticipantSummary;
import com.traininghub.enrollment.repository.EnrollmentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClientException;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.UUID;

@Service
public class EnrollmentService {
    private static final EnumSet<EnrollmentStatus> ACTIVE_STATUSES =
            EnumSet.of(EnrollmentStatus.REQUESTED, EnrollmentStatus.CONFIRMED);

    private final EnrollmentRepository repository;
    private final CourseClient courseClient;
    private final ParticipantClient participantClient;

    /** Crea il servizio iscrizioni e inietta i client dei microservizi proprietari dei dati. */
    public EnrollmentService(EnrollmentRepository repository, CourseClient courseClient,
                             ParticipantClient participantClient) {
        this.repository = repository;
        this.courseClient = courseClient;
        this.participantClient = participantClient;
    }

    @Transactional
    /** Crea un'iscrizione richiesta dopo aver verificato partecipante, corso, duplicati e capienza. */
    public Enrollment create(EnrollmentRequest request) {
        CourseSummary course = getCourse(request.getCourseId());
        ParticipantSummary participant = getParticipant(request.getParticipantId());
        if (!Boolean.TRUE.equals(participant.active())) {
            throw new EnrollmentDependencyException("Participant is not active", null);
        }
        if (repository.existsByCourseIdAndParticipantIdAndStatusIn(
                request.getCourseId(), request.getParticipantId(), ACTIVE_STATUSES)) {
            throw new DuplicateEnrollmentException();
        }
        if (repository.countByCourseIdAndStatusIn(request.getCourseId(), ACTIVE_STATUSES)
                >= course.maximumCapacity()) {
            throw new CourseCapacityExceededException();
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setCourseId(request.getCourseId());
        enrollment.setParticipantId(request.getParticipantId());
        enrollment.setEnrollmentDate(LocalDate.now());
        enrollment.setStatus(EnrollmentStatus.REQUESTED);
        return repository.save(enrollment);
    }

    @Transactional(readOnly = true)
    /** Carica un'iscrizione per UUID oppure segnala che non esiste. */
    public Enrollment getById(UUID id) {
        return repository.findById(id).orElseThrow(() -> new EnrollmentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    /** Elenca le iscrizioni appartenenti a un corso. */
    public Page<Enrollment> findByCourse(UUID courseId, Pageable pageable) {
        return repository.findByCourseId(courseId, pageable);
    }

    @Transactional(readOnly = true)
    /** Elenca i corsi ai quali un partecipante e' iscritto. */
    public Page<Enrollment> findByParticipant(UUID participantId, Pageable pageable) {
        return repository.findByParticipantId(participantId, pageable);
    }

    @Transactional
    /** Modifica lo stato del ciclo di vita di un'iscrizione esistente. */
    public Enrollment updateStatus(UUID id, EnrollmentStatus status) {
        Enrollment enrollment = getById(id);
        enrollment.setStatus(status);
        return repository.save(enrollment);
    }

    /** Chiama course-service e converte gli errori remoti in un errore di dominio. */
    private CourseSummary getCourse(UUID courseId) {
        try {
            CourseSummary course = courseClient.findById(courseId);
            if (course == null || course.maximumCapacity() == null) {
                throw new EnrollmentDependencyException("Course was not found", null);
            }
            return course;
        } catch (RestClientException exception) {
            throw new EnrollmentDependencyException("Course service is unavailable", exception);
        }
    }

    /** Chiama participant-service e converte gli errori remoti in un errore di dominio. */
    private ParticipantSummary getParticipant(UUID participantId) {
        try {
            ParticipantSummary participant = participantClient.findById(participantId);
            if (participant == null) throw new EnrollmentDependencyException("Participant was not found", null);
            return participant;
        } catch (RestClientException exception) {
            throw new EnrollmentDependencyException("Participant service is unavailable", exception);
        }
    }
}