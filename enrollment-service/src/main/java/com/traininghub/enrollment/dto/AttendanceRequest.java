package com.traininghub.enrollment.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public class AttendanceRequest {
    @NotNull private UUID enrollmentId;
    @NotNull private LocalDate lessonDate;
    private LocalTime entryTime;
    private LocalTime exitTime;
    private boolean absent;
    @Size(max = 500) private String justification;

    public UUID getEnrollmentId() { return enrollmentId; }
    public void setEnrollmentId(UUID value) { enrollmentId = value; }
    public LocalDate getLessonDate() { return lessonDate; }
    public void setLessonDate(LocalDate value) { lessonDate = value; }
    public LocalTime getEntryTime() { return entryTime; }
    public void setEntryTime(LocalTime value) { entryTime = value; }
    public LocalTime getExitTime() { return exitTime; }
    public void setExitTime(LocalTime value) { exitTime = value; }
    public boolean isAbsent() { return absent; }
    public void setAbsent(boolean value) { absent = value; }
    public String getJustification() { return justification; }
    public void setJustification(String value) { justification = value; }
}