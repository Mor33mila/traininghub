package com.traininghub.enrollment.repository;

import com.traininghub.enrollment.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    /** Verifica se la stessa iscrizione ha gia' una presenza per quella data. */
    boolean existsByEnrollmentIdAndLessonDate(UUID enrollmentId, java.time.LocalDate lessonDate);
    /** Elenca le lezioni di un'iscrizione in ordine cronologico. */
    List<Attendance> findByEnrollmentIdOrderByLessonDateAsc(UUID enrollmentId);
    @Query("select a from Attendance a where a.enrollmentId in " +
            "(select e.id from Enrollment e where e.courseId = :courseId) order by a.lessonDate asc")
    /** Elenca le presenze attraverso le iscrizioni appartenenti a un corso. */
    List<Attendance> findByCourseId(@Param("courseId") UUID courseId);
    @Query("select coalesce(sum(a.attendedHours), 0) from Attendance a where a.enrollmentId = :enrollmentId")
    /** Somma le ore frequentate per i calcoli di frequenza. */
    BigDecimal sumAttendedHours(@Param("enrollmentId") UUID enrollmentId);
}