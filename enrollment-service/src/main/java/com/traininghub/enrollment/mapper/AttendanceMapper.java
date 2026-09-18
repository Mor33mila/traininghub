package com.traininghub.enrollment.mapper;

import com.traininghub.enrollment.dto.AttendanceResponse;
import com.traininghub.enrollment.entity.Attendance;
import org.springframework.stereotype.Component;

@Component
public class AttendanceMapper {
    /** Converte un'entity presenza nel DTO di risposta REST. */
    public AttendanceResponse toResponse(Attendance attendance) {
        return new AttendanceResponse(attendance.getId(), attendance.getEnrollmentId(), attendance.getLessonDate(),
                attendance.getEntryTime(), attendance.getExitTime(), attendance.getAttendedHours(),
                attendance.getAbsent(), attendance.getJustification());
    }
}