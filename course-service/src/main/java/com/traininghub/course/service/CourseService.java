package com.traininghub.course.service;

import com.traininghub.course.dto.CourseRequest;
import com.traininghub.course.entity.Course;
import com.traininghub.course.entity.CourseStatus;
import com.traininghub.course.exception.CourseNotFoundException;
import com.traininghub.course.exception.DuplicateCourseCodeException;
import com.traininghub.course.exception.InvalidCourseDatesException;
import com.traininghub.course.mapper.CourseMapper;
import com.traininghub.course.repository.CourseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CourseService {
    private final CourseRepository courseRepository;
    private final CourseMapper courseMapper;

    /** Crea il servizio applicativo con le dipendenze di persistenza e mapping. */
    public CourseService(CourseRepository courseRepository, CourseMapper courseMapper) {
        this.courseRepository = courseRepository;
        this.courseMapper = courseMapper;
    }

    @Transactional
    /** Valida e salva un nuovo corso dopo aver verificato l'unicita' del codice. */
    public Course create(CourseRequest request) {
        validateDates(request);
        String code = request.getCourseCode().trim();
        if (courseRepository.existsByCourseCodeIgnoreCase(code)) {
            throw new DuplicateCourseCodeException(request.getCourseCode());
        }
        return courseRepository.save(courseMapper.toEntity(request));
    }

    @Transactional(readOnly = true)
    /** Carica un corso oppure solleva l'eccezione standard di risorsa non trovata. */
    public Course getById(UUID id) {
        return courseRepository.findById(id).orElseThrow(() -> new CourseNotFoundException(id));
    }

    @Transactional(readOnly = true)
    /** Cerca i corsi per titolo o codice, oppure restituisce la pagina richiesta senza filtro. */
    public Page<Course> search(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return courseRepository.findAll(pageable);
        }
        String value = query.trim();
        return courseRepository.findByTitleContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(
                value, value, pageable);
    }

    /** Cerca solo i corsi assegnati al docente autenticato. */
    @Transactional(readOnly = true)
    public Page<Course> searchByInstructor(UUID instructorId, String query, Pageable pageable) {
        if (query == null || query.isBlank()) return courseRepository.findByInstructorId(instructorId, pageable);
        String value = query.trim();
        return courseRepository.findByInstructorIdAndTitleContainingIgnoreCaseOrInstructorIdAndCourseCodeContainingIgnoreCase(
                instructorId, value, instructorId, value, pageable);
    }

    @Transactional(readOnly = true)
    /** Restituisce i corsi che hanno lo stato richiesto. */
    public Page<Course> findByStatus(CourseStatus status, Pageable pageable) {
        return courseRepository.findByStatus(status, pageable);
    }

    /** Restituisce uno stato solo tra i corsi assegnati al docente. */
    @Transactional(readOnly = true)
    public Page<Course> findByInstructorAndStatus(UUID instructorId, CourseStatus status, Pageable pageable) {
        return courseRepository.findByInstructorIdAndStatus(instructorId, status, pageable);
    }

    /** Carica un corso per un docente verificando l'assegnazione. */
    @Transactional(readOnly = true)
    public Course getByIdForInstructor(UUID id, UUID instructorId) {
        Course course = getById(id);
        if (!instructorId.equals(course.getInstructorId())) {
            throw new org.springframework.security.access.AccessDeniedException("Course is not assigned to this teacher");
        }
        return course;
    }

    @Transactional
    /** Valida e sostituisce i campi modificabili di un corso esistente. */
    public Course update(UUID id, CourseRequest request) {
        validateDates(request);
        Course course = getById(id);
        String code = request.getCourseCode().trim();
        if (courseRepository.existsByCourseCodeIgnoreCaseAndIdNot(code, id)) {
            throw new DuplicateCourseCodeException(request.getCourseCode());
        }
        courseMapper.updateEntity(course, request);
        return courseRepository.save(course);
    }

    @Transactional
    /** Elimina il corso indicato dall'UUID dopo aver verificato che esista. */
    public void delete(UUID id) {
        courseRepository.delete(getById(id));
    }

    /** Applica la regola per cui un corso non puo' terminare prima dell'inizio. */
    private void validateDates(CourseRequest request) {
        if (request.getStartDate() != null && request.getEndDate() != null
                && request.getEndDate().isBefore(request.getStartDate())) {
            throw new InvalidCourseDatesException();
        }
    }
}