package com.traininghub.enrollment.repository;

import com.traininghub.enrollment.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface LessonRepository extends JpaRepository<Lesson, UUID> {
    List<Lesson> findByCourseIdAndLessonDateBetweenOrderByLessonDateAscStartTimeAsc(
            UUID courseId, LocalDate from, LocalDate to);
}