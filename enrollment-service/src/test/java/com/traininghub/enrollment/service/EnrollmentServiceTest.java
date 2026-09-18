package com.traininghub.enrollment.service;

import com.traininghub.enrollment.dto.EnrollmentRequest;
import com.traininghub.enrollment.entity.Enrollment;
import com.traininghub.enrollment.exception.CourseCapacityExceededException;
import com.traininghub.enrollment.exception.DuplicateEnrollmentException;
import com.traininghub.enrollment.integration.CourseClient;
import com.traininghub.enrollment.integration.CourseSummary;
import com.traininghub.enrollment.integration.ParticipantClient;
import com.traininghub.enrollment.integration.ParticipantSummary;
import com.traininghub.enrollment.repository.EnrollmentRepository;
import org.junit.jupiter.api.Test;
import java.util.EnumSet;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static com.traininghub.enrollment.entity.EnrollmentStatus.*;

class EnrollmentServiceTest {
    private final EnrollmentRepository repository = mock(EnrollmentRepository.class);
    private final CourseClient courseClient = mock(CourseClient.class);
    private final ParticipantClient participantClient = mock(ParticipantClient.class);
    private final EnrollmentService service = new EnrollmentService(repository, courseClient, participantClient);
    private final UUID courseId = UUID.randomUUID();
    private final UUID participantId = UUID.randomUUID();

    @Test
    void rejectsDuplicateActiveEnrollment() {
        givenDependencies(10);
        when(repository.existsByCourseIdAndParticipantIdAndStatusIn(eq(courseId), eq(participantId), any()))
                .thenReturn(true);
        assertThrows(DuplicateEnrollmentException.class, () -> service.create(request()));
    }

    @Test
    void rejectsEnrollmentWhenCourseIsFull() {
        givenDependencies(1);
        when(repository.existsByCourseIdAndParticipantIdAndStatusIn(eq(courseId), eq(participantId), any()))
                .thenReturn(false);
        when(repository.countByCourseIdAndStatusIn(eq(courseId), any())).thenReturn(1L);
        assertThrows(CourseCapacityExceededException.class, () -> service.create(request()));
    }

    private void givenDependencies(int capacity) {
        when(courseClient.findById(courseId)).thenReturn(new CourseSummary(courseId, capacity));
        when(participantClient.findById(participantId)).thenReturn(new ParticipantSummary(participantId, true));
    }

    private EnrollmentRequest request() {
        EnrollmentRequest request = new EnrollmentRequest();
        request.setCourseId(courseId);
        request.setParticipantId(participantId);
        return request;
    }
}