package com.traininghub.enrollment.service;

import com.traininghub.enrollment.dto.AttendanceRequest;
import com.traininghub.enrollment.entity.Attendance;
import com.traininghub.enrollment.entity.Enrollment;
import com.traininghub.enrollment.entity.EnrollmentStatus;
import com.traininghub.enrollment.exception.InvalidAttendanceException;
import com.traininghub.enrollment.integration.CourseClient;
import com.traininghub.enrollment.integration.CourseSummary;
import com.traininghub.enrollment.repository.AttendanceRepository;
import com.traininghub.enrollment.repository.EnrollmentRepository;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AttendanceServiceTest {
    private final AttendanceRepository attendanceRepository = mock(AttendanceRepository.class);
    private final EnrollmentRepository enrollmentRepository = mock(EnrollmentRepository.class);
    private final CourseClient courseClient = mock(CourseClient.class);
    private final AttendanceService service = new AttendanceService(attendanceRepository, enrollmentRepository,
            courseClient, BigDecimal.valueOf(80));
    private final UUID enrollmentId = UUID.randomUUID();
    private final UUID courseId = UUID.randomUUID();

    @Test
    void calculatesAttendedHoursFromEntryAndExit() {
        Enrollment enrollment = confirmedEnrollment();
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(courseClient.findById(courseId)).thenReturn(new CourseSummary(courseId, 10,
                BigDecimal.TEN, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));
        when(attendanceRepository.existsByEnrollmentIdAndLessonDate(any(), any())).thenReturn(false);
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Attendance attendance = service.create(request(false));

        assertEquals(0, BigDecimal.valueOf(2.5).compareTo(attendance.getAttendedHours()));
    }

    @Test
    void rejectsPresentAttendanceWithoutTimes() {
        Enrollment enrollment = confirmedEnrollment();
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(courseClient.findById(courseId)).thenReturn(new CourseSummary(courseId, 10,
                BigDecimal.TEN, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));

        AttendanceRequest request = request(false);
        request.setEntryTime(null);
        assertThrows(InvalidAttendanceException.class, () -> service.create(request));
    }

    @Test
    void recordsAbsenceWithoutEntryOrExitTimes() {
        Enrollment enrollment = confirmedEnrollment();
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(enrollment));
        when(courseClient.findById(courseId)).thenReturn(new CourseSummary(courseId, 10,
                BigDecimal.TEN, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));
        when(attendanceRepository.existsByEnrollmentIdAndLessonDate(any(), any())).thenReturn(false);
        when(attendanceRepository.save(any(Attendance.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AttendanceRequest request = request(true);
        request.setEntryTime(null);
        request.setExitTime(null);

        Attendance attendance = service.create(request);

        assertEquals(true, attendance.getAbsent());
        assertEquals(0, BigDecimal.ZERO.compareTo(attendance.getAttendedHours()));
    }

    @Test
    void excludesRequestedAndWithdrawnEnrollmentsFromParticipantFrequency() {
        UUID participantId = UUID.randomUUID();
        Enrollment confirmed = confirmedEnrollment();
        confirmed.setParticipantId(participantId);
        Enrollment requested = new Enrollment();
        requested.setId(UUID.randomUUID());
        requested.setCourseId(UUID.randomUUID());
        requested.setParticipantId(participantId);
        requested.setStatus(EnrollmentStatus.REQUESTED);
        Enrollment withdrawn = new Enrollment();
        withdrawn.setId(UUID.randomUUID());
        withdrawn.setCourseId(UUID.randomUUID());
        withdrawn.setParticipantId(participantId);
        withdrawn.setStatus(EnrollmentStatus.WITHDRAWN);
        when(enrollmentRepository.findAllByParticipantId(participantId))
            .thenReturn(List.of(confirmed, requested, withdrawn));
        when(courseClient.findById(courseId)).thenReturn(new CourseSummary(courseId, 10,
                BigDecimal.TEN, LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)));
        when(attendanceRepository.sumAttendedHours(enrollmentId)).thenReturn(BigDecimal.valueOf(7));

        var frequency = service.calculateParticipantFrequency(participantId);

        assertEquals(0, BigDecimal.TEN.compareTo(frequency.totalHours()));
        assertEquals(0, BigDecimal.valueOf(7).compareTo(frequency.attendedHours()));
        assertEquals(0, BigDecimal.valueOf(70).compareTo(frequency.percentage()));
    }

    private Enrollment confirmedEnrollment() {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(enrollmentId);
        enrollment.setCourseId(courseId);
        enrollment.setParticipantId(UUID.randomUUID());
        enrollment.setStatus(EnrollmentStatus.CONFIRMED);
        return enrollment;
    }

    private AttendanceRequest request(boolean absent) {
        AttendanceRequest request = new AttendanceRequest();
        request.setEnrollmentId(enrollmentId);
        request.setLessonDate(LocalDate.of(2026, 5, 10));
        request.setEntryTime(LocalTime.of(9, 0));
        request.setExitTime(LocalTime.of(11, 30));
        request.setAbsent(absent);
        return request;
    }
}