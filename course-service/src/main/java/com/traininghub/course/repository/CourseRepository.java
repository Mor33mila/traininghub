package com.traininghub.course.repository;

import com.traininghub.course.entity.Course;
import com.traininghub.course.entity.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {

    boolean existsByCourseCodeIgnoreCase(String courseCode);

    boolean existsByCourseCodeIgnoreCaseAndIdNot(String courseCode, UUID id);

    Page<Course> findByTitleContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(
            String title, String courseCode, Pageable pageable);

    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    Page<Course> findByInstructorId(UUID instructorId, Pageable pageable);

    Page<Course> findByInstructorIdAndTitleContainingIgnoreCaseOrInstructorIdAndCourseCodeContainingIgnoreCase(
            UUID instructorId, String title, UUID sameInstructorId, String courseCode, Pageable pageable);

    Page<Course> findByInstructorIdAndStatus(UUID instructorId, CourseStatus status, Pageable pageable);
}