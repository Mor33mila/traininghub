package com.traininghub.enrollment.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "attendance", indexes = {
        @Index(name = "idx_attendance_enrollment", columnList = "enrollment_id"),
        @Index(name = "idx_attendance_lesson_date", columnList = "lesson_date")
})
public class Attendance {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @NotNull @Column(name = "enrollment_id", nullable = false)
    private UUID enrollmentId;
    @NotNull @Column(name = "lesson_date", nullable = false)
    private LocalDate lessonDate;
    @Column(name = "entry_time")
    private LocalTime entryTime;
    @Column(name = "exit_time")
    private LocalTime exitTime;
    @NotNull @PositiveOrZero @Column(name = "attended_hours", nullable = false, precision = 6, scale = 2)
    private BigDecimal attendedHours = BigDecimal.ZERO;
    @NotNull @Column(nullable = false)
    private Boolean absent;
    @Column(length = 500)
    private String justification;

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public UUID getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(UUID enrollmentId) { this.enrollmentId = enrollmentId; }
    public LocalDate getLessonDate() { return lessonDate; }
    public void setLessonDate(LocalDate lessonDate) { this.lessonDate = lessonDate; }
    public LocalTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalTime entryTime) { this.entryTime = entryTime; }
    public LocalTime getExitTime() { return exitTime; }
    public void setExitTime(LocalTime exitTime) { this.exitTime = exitTime; }
    public BigDecimal getAttendedHours() { return attendedHours; }
    public void setAttendedHours(BigDecimal attendedHours) { this.attendedHours = attendedHours; }
    public Boolean getAbsent() { return absent; }
    public void setAbsent(Boolean absent) { this.absent = absent; }
    public String getJustification() { return justification; }
    public void setJustification(String justification) { this.justification = justification; }
}