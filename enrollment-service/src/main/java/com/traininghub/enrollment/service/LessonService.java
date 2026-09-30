package com.traininghub.enrollment.service;

import com.traininghub.enrollment.dto.LessonRequest;
import com.traininghub.enrollment.entity.Lesson;
import com.traininghub.enrollment.exception.EnrollmentDependencyException;
import com.traininghub.enrollment.exception.InvalidLessonException;
import com.traininghub.enrollment.exception.LessonNotFoundException;
import com.traininghub.enrollment.integration.CourseClient;
import com.traininghub.enrollment.integration.CourseSummary;
import com.traininghub.enrollment.repository.LessonRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class LessonService {
    private final LessonRepository repository;
    private final CourseClient courseClient;

    public LessonService(LessonRepository repository, CourseClient courseClient) {
        this.repository = repository;
        this.courseClient = courseClient;
    }

    @Transactional(readOnly = true)
    public List<Lesson> findByCourse(UUID courseId, LocalDate from, LocalDate to, UUID instructorId) {
        if (from.isAfter(to)) throw new InvalidLessonException("Start date must not be after end date");
        CourseSummary course = getCourse(courseId);
        verifyInstructor(course, instructorId);
        return repository.findByCourseIdAndLessonDateBetweenOrderByLessonDateAscStartTimeAsc(courseId, from, to);
    }

    @Transactional
    public Lesson create(LessonRequest request) {
        CourseSummary course = getCourse(request.courseId());
        validateAgainstCourse(request, course);
        Lesson lesson = new Lesson();
        apply(lesson, request);
        return repository.save(lesson);
    }

    @Transactional
    public Lesson update(UUID id, LessonRequest request) {
        Lesson lesson = repository.findById(id).orElseThrow(() -> new LessonNotFoundException(id));
        CourseSummary course = getCourse(request.courseId());
        validateAgainstCourse(request, course);
        apply(lesson, request);
        return repository.save(lesson);
    }

    @Transactional
    public void delete(UUID id) {
        Lesson lesson = repository.findById(id).orElseThrow(() -> new LessonNotFoundException(id));
        repository.delete(lesson);
    }

    private CourseSummary getCourse(UUID courseId) {
        try {
            CourseSummary course = courseClient.findById(courseId);
            if (course == null) throw new InvalidLessonException("Course was not found");
            return course;
        } catch (HttpClientErrorException exception) {
            if (exception.getStatusCode().value() == 403) {
                throw new AccessDeniedException("Course is not assigned to this teacher", exception);
            }
            if (exception.getStatusCode().value() == 404) {
                throw new InvalidLessonException("Course was not found");
            }
            throw new EnrollmentDependencyException("Course service is unavailable", exception);
        } catch (RestClientException exception) {
            throw new EnrollmentDependencyException("Course service is unavailable", exception);
        }
    }

    private void validateAgainstCourse(LessonRequest request, CourseSummary course) {
        if (course.startDate() != null && request.lessonDate().isBefore(course.startDate())
                || course.endDate() != null && request.lessonDate().isAfter(course.endDate())) {
            throw new InvalidLessonException("Lesson date must be within the course period");
        }
        if (!request.endTime().isAfter(request.startTime())) {
            throw new InvalidLessonException("End time must be later than start time");
        }
    }

    private void verifyInstructor(CourseSummary course, UUID instructorId) {
        if (instructorId != null && !instructorId.equals(course.instructorId())) {
            throw new AccessDeniedException("Course is not assigned to this teacher");
        }
    }

    private void apply(Lesson lesson, LessonRequest request) {
        lesson.setCourseId(request.courseId());
        lesson.setTitle(request.title().trim());
        lesson.setLessonDate(request.lessonDate());
        lesson.setStartTime(request.startTime());
        lesson.setEndTime(request.endTime());
        lesson.setNotes(request.notes());
    }
}