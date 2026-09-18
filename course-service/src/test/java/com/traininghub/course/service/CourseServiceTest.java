package com.traininghub.course.service;

import com.traininghub.course.dto.CourseRequest;
import com.traininghub.course.entity.Course;
import com.traininghub.course.entity.CourseMode;
import com.traininghub.course.entity.CourseStatus;
import com.traininghub.course.exception.DuplicateCourseCodeException;
import com.traininghub.course.exception.InvalidCourseDatesException;
import com.traininghub.course.mapper.CourseMapper;
import com.traininghub.course.repository.CourseRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CourseServiceTest {

    private final CourseRepository courseRepository = mock(CourseRepository.class);
    private final CourseService courseService = new CourseService(courseRepository, new CourseMapper());

    @Test
    void rejectsDuplicateCourseCode() {
        CourseRequest request = validRequest();
        when(courseRepository.existsByCourseCodeIgnoreCase("JAVA-101")).thenReturn(true);

        assertThrows(DuplicateCourseCodeException.class, () -> courseService.create(request));
    }

    @Test
    void rejectsEndDateBeforeStartDate() {
        CourseRequest request = validRequest();
        request.setEndDate(LocalDate.of(2026, 9, 1));

        assertThrows(InvalidCourseDatesException.class, () -> courseService.create(request));
    }

    @Test
    void mapsAndSavesValidCourse() {
        CourseRequest request = validRequest();
        when(courseRepository.existsByCourseCodeIgnoreCase("JAVA-101")).thenReturn(false);
        when(courseRepository.save(any(Course.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Course result = courseService.create(request);

        org.junit.jupiter.api.Assertions.assertEquals("JAVA-101", result.getCourseCode());
        org.junit.jupiter.api.Assertions.assertEquals(CourseStatus.SCHEDULED, result.getStatus());
    }

    private CourseRequest validRequest() {
        CourseRequest request = new CourseRequest();
        request.setCourseCode(" JAVA-101 ");
        request.setTitle("Java Fundamentals");
        request.setTotalHours(BigDecimal.valueOf(24));
        request.setStartDate(LocalDate.of(2026, 10, 1));
        request.setEndDate(LocalDate.of(2026, 10, 3));
        request.setMaximumCapacity(15);
        request.setMode(CourseMode.MIXED);
        request.setStatus(CourseStatus.SCHEDULED);
        return request;
    }
}