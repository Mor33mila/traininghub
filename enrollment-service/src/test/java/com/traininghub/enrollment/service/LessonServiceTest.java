package com.traininghub.enrollment.service;

import com.traininghub.enrollment.dto.LessonRequest;
import com.traininghub.enrollment.entity.Lesson;
import com.traininghub.enrollment.exception.InvalidLessonException;
import com.traininghub.enrollment.integration.CourseClient;
import com.traininghub.enrollment.integration.CourseSummary;
import com.traininghub.enrollment.repository.LessonRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LessonServiceTest {
    private final LessonRepository repository = mock(LessonRepository.class);
    private final CourseClient courseClient = mock(CourseClient.class);
    private final LessonService service = new LessonService(repository, courseClient);
    private final UUID courseId = UUID.randomUUID();

    @Test
    void createsLessonWithinCoursePeriod() {
        when(courseClient.findById(courseId)).thenReturn(course());
        when(repository.save(any(Lesson.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Lesson lesson = service.create(request(LocalDate.of(2026, 10, 10), LocalTime.of(9, 0), LocalTime.of(11, 0)));

        assertEquals(courseId, lesson.getCourseId());
        assertEquals("Introduzione", lesson.getTitle());
        verify(repository).save(any(Lesson.class));
    }

    @Test
    void rejectsLessonOutsideCoursePeriod() {
        when(courseClient.findById(courseId)).thenReturn(course());

        assertThrows(InvalidLessonException.class, () -> service.create(
                request(LocalDate.of(2026, 12, 1), LocalTime.of(9, 0), LocalTime.of(11, 0))));
    }

    @Test
    void rejectsLessonWithEndTimeBeforeStartTime() {
        when(courseClient.findById(courseId)).thenReturn(course());

        assertThrows(InvalidLessonException.class, () -> service.create(
                request(LocalDate.of(2026, 10, 10), LocalTime.of(11, 0), LocalTime.of(9, 0))));
    }

    @Test
    void deniesTeacherWhenCourseIsNotAssigned() {
        UUID instructorId = UUID.randomUUID();
        when(courseClient.findById(courseId)).thenReturn(course());

        assertThrows(AccessDeniedException.class, () -> service.findByCourse(courseId,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), instructorId));
    }

    @Test
    void preservesForbiddenResponseFromCourseService() {
        when(courseClient.findById(courseId)).thenThrow(new HttpClientErrorException(HttpStatus.FORBIDDEN));

        assertThrows(AccessDeniedException.class, () -> service.findByCourse(courseId,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), UUID.randomUUID()));
    }

    private CourseSummary course() {
        return new CourseSummary(courseId, 12, BigDecimal.TEN,
                LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), UUID.randomUUID());
    }

    private LessonRequest request(LocalDate date, LocalTime start, LocalTime end) {
        return new LessonRequest(courseId, " Introduzione ", date, start, end, "Aula 2");
    }
}
