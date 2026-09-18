package com.traininghub.course.mapper;

import com.traininghub.course.dto.CourseRequest;
import com.traininghub.course.dto.CourseResponse;
import com.traininghub.course.entity.Course;
import org.springframework.stereotype.Component;

@Component
public class CourseMapper {
    /** Crea un'entity di persistenza dalla richiesta API in ingresso. */
    public Course toEntity(CourseRequest request) {
        Course course = new Course();
        updateEntity(course, request);
        return course;
    }

    /** Copia i valori della richiesta nell'entity e normalizza i campi testuali. */
    public void updateEntity(Course course, CourseRequest request) {
        course.setCourseCode(request.getCourseCode().trim());
        course.setTitle(request.getTitle().trim());
        course.setDescription(request.getDescription());
        course.setTrainingArea(request.getTrainingArea());
        course.setTotalHours(request.getTotalHours());
        course.setStartDate(request.getStartDate());
        course.setEndDate(request.getEndDate());
        course.setMaximumCapacity(request.getMaximumCapacity());
        course.setMode(request.getMode());
        course.setStatus(request.getStatus());
        course.setInstructorId(request.getInstructorId());
    }

    /** Converte un'entity nel DTO di risposta esposto dall'API REST. */
    public CourseResponse toResponse(Course course) {
        return new CourseResponse(course.getId(), course.getCourseCode(), course.getTitle(),
                course.getDescription(), course.getTrainingArea(), course.getTotalHours(),
                course.getStartDate(), course.getEndDate(), course.getMaximumCapacity(),
                course.getMode(), course.getStatus(), course.getInstructorId());
    }
}