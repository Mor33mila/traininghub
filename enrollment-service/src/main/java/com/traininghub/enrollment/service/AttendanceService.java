package com.traininghub.enrollment.service;

import com.traininghub.enrollment.dto.AttendanceRequest;
import com.traininghub.enrollment.dto.FrequencyResponse;
import com.traininghub.enrollment.dto.ParticipantFrequencyResponse;
import com.traininghub.enrollment.entity.Attendance;
import com.traininghub.enrollment.entity.Enrollment;
import com.traininghub.enrollment.entity.EnrollmentStatus;
import com.traininghub.enrollment.exception.DuplicateAttendanceException;
import com.traininghub.enrollment.exception.InvalidAttendanceException;
import com.traininghub.enrollment.integration.CourseClient;
import com.traininghub.enrollment.integration.CourseSummary;
import com.traininghub.enrollment.repository.AttendanceRepository;
import com.traininghub.enrollment.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

@Service
public class AttendanceService {
    private final AttendanceRepository attendanceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final CourseClient courseClient;
    private final BigDecimal minimumThreshold;

    /** Crea il servizio presenze e carica la soglia minima configurata. */
    public AttendanceService(AttendanceRepository attendanceRepository, EnrollmentRepository enrollmentRepository,
                             CourseClient courseClient,
                             @Value("${attendance.minimum-threshold:80}") BigDecimal minimumThreshold) {
        this.attendanceRepository = attendanceRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.courseClient = courseClient;
        this.minimumThreshold = minimumThreshold;
    }

    @Transactional
    /** Registra una presenza dopo aver validato iscrizione, data, orari e duplicati. */
    public Attendance create(AttendanceRequest request) {
        Enrollment enrollment = enrollmentRepository.findById(request.getEnrollmentId())
                .orElseThrow(() -> new InvalidAttendanceException("Enrollment was not found"));
        if (enrollment.getStatus() != EnrollmentStatus.CONFIRMED
                && enrollment.getStatus() != EnrollmentStatus.COMPLETED) {
            throw new InvalidAttendanceException("Attendance requires a confirmed enrollment");
        }
        CourseSummary course = courseClient.findById(enrollment.getCourseId());
        if (course.startDate() != null && request.getLessonDate().isBefore(course.startDate())
                || course.endDate() != null && request.getLessonDate().isAfter(course.endDate())) {
            throw new InvalidAttendanceException("Lesson date must be within the course period");
        }
        if (attendanceRepository.existsByEnrollmentIdAndLessonDate(request.getEnrollmentId(), request.getLessonDate())) {
            throw new DuplicateAttendanceException();
        }
        BigDecimal attendedHours = calculateHours(request);
        Attendance attendance = new Attendance();
        attendance.setEnrollmentId(request.getEnrollmentId());
        attendance.setLessonDate(request.getLessonDate());
        attendance.setEntryTime(request.getEntryTime());
        attendance.setExitTime(request.getExitTime());
        attendance.setAttendedHours(attendedHours);
        attendance.setAbsent(request.isAbsent());
        attendance.setJustification(request.getJustification());
        return attendanceRepository.save(attendance);
    }

    @Transactional(readOnly = true)
    /** Restituisce le presenze di tutte le iscrizioni appartenenti a un corso. */
    public List<Attendance> findByCourse(UUID courseId, UUID instructorId) {
        if (instructorId != null) {
            CourseSummary course = courseClient.findById(courseId);
            if (course.instructorId() == null || !instructorId.equals(course.instructorId())) {
                throw new org.springframework.security.access.AccessDeniedException("Course is not assigned to this teacher");
            }
        }
        return attendanceRepository.findByCourseId(courseId);
    }

    @Transactional(readOnly = true)
    /** Calcola la percentuale di frequenza per una singola iscrizione. */
    public FrequencyResponse calculateFrequency(UUID participantId, UUID enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new InvalidAttendanceException("Enrollment was not found"));
        if (!enrollment.getParticipantId().equals(participantId)) {
            throw new InvalidAttendanceException("Enrollment does not belong to participant");
        }
        CourseSummary course = courseClient.findById(enrollment.getCourseId());
        BigDecimal totalHours = course.totalHours();
        if (totalHours == null || totalHours.signum() <= 0) {
            throw new InvalidAttendanceException("Course total hours must be positive");
        }
        BigDecimal attended = attendanceRepository.sumAttendedHours(enrollmentId);
        BigDecimal percentage = attended.multiply(BigDecimal.valueOf(100))
                .divide(totalHours, 2, RoundingMode.HALF_UP);
        BigDecimal absent = totalHours.subtract(attended).max(BigDecimal.ZERO);
        return new FrequencyResponse(participantId, enrollmentId, attended, absent, totalHours, percentage,
                percentage.compareTo(minimumThreshold) < 0);
    }

    @Transactional(readOnly = true)
    /** Aggrega ore frequentate e percentuale su tutti i corsi del partecipante. */
    public ParticipantFrequencyResponse calculateParticipantFrequency(UUID participantId) {
        BigDecimal attendedTotal = BigDecimal.ZERO;
        BigDecimal courseHoursTotal = BigDecimal.ZERO;
        for (Enrollment enrollment : enrollmentRepository.findAllByParticipantId(participantId)) {
            CourseSummary course = courseClient.findById(enrollment.getCourseId());
            if (course.totalHours() == null || course.totalHours().signum() <= 0) continue;
            attendedTotal = attendedTotal.add(attendanceRepository.sumAttendedHours(enrollment.getId()));
            courseHoursTotal = courseHoursTotal.add(course.totalHours());
        }
        BigDecimal percentage = courseHoursTotal.signum() == 0 ? BigDecimal.ZERO
                : attendedTotal.multiply(BigDecimal.valueOf(100))
                .divide(courseHoursTotal, 2, RoundingMode.HALF_UP);
        BigDecimal absentTotal = courseHoursTotal.subtract(attendedTotal).max(BigDecimal.ZERO);
        return new ParticipantFrequencyResponse(participantId, attendedTotal, absentTotal, courseHoursTotal, percentage,
                percentage.compareTo(minimumThreshold) < 0);
    }

    /** Converte gli orari in ore oppure restituisce zero in caso di assenza. */
    private BigDecimal calculateHours(AttendanceRequest request) {
        if (request.isAbsent()) {
            if (request.getEntryTime() != null || request.getExitTime() != null) {
                throw new InvalidAttendanceException("Absent attendance cannot have entry or exit time");
            }
            return BigDecimal.ZERO;
        }
        if (request.getEntryTime() == null || request.getExitTime() == null) {
            throw new InvalidAttendanceException("Present attendance requires entry and exit time");
        }
        if (!request.getExitTime().isAfter(request.getEntryTime())) {
            throw new InvalidAttendanceException("Exit time must be later than entry time");
        }
        return BigDecimal.valueOf(Duration.between(request.getEntryTime(), request.getExitTime()).toMinutes())
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }
}