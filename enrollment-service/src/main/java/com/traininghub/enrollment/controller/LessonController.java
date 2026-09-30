package com.traininghub.enrollment.controller;

import com.traininghub.enrollment.dto.LessonRequest;
import com.traininghub.enrollment.dto.LessonResponse;
import com.traininghub.enrollment.entity.Lesson;
import com.traininghub.enrollment.service.LessonService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {
    private final LessonService service;

    public LessonController(LessonService service) {
        this.service = service;
    }

    @GetMapping("/course/{courseId}")
    public List<LessonResponse> byCourse(@PathVariable UUID courseId,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                         @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                                         @AuthenticationPrincipal Jwt jwt) {
        UUID instructorId = isTeacher(jwt) ? UUID.fromString(jwt.getSubject()) : null;
        return service.findByCourse(courseId, from, to, instructorId).stream().map(this::toResponse).toList();
    }

    @PostMapping
    public ResponseEntity<LessonResponse> create(@Valid @RequestBody LessonRequest request) {
        return ResponseEntity.status(201).body(toResponse(service.create(request)));
    }

    @PutMapping("/{id}")
    public LessonResponse update(@PathVariable UUID id, @Valid @RequestBody LessonRequest request) {
        return toResponse(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isTeacher(Jwt jwt) {
        return jwt != null && jwt.getClaimAsStringList("roles") != null
                && jwt.getClaimAsStringList("roles").contains("TEACHER");
    }

    private LessonResponse toResponse(Lesson lesson) {
        return new LessonResponse(lesson.getId(), lesson.getCourseId(), lesson.getTitle(),
                lesson.getLessonDate(), lesson.getStartTime(), lesson.getEndTime(), lesson.getNotes());
    }
}