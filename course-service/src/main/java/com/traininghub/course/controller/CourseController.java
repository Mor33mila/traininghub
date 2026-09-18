package com.traininghub.course.controller;

import com.traininghub.course.dto.CourseRequest;
import com.traininghub.course.dto.CourseResponse;
import com.traininghub.course.entity.CourseStatus;
import com.traininghub.course.mapper.CourseMapper;
import com.traininghub.course.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/courses")
public class CourseController {
    private final CourseService courseService;
    private final CourseMapper courseMapper;

    /** Crea l'adapter REST con il servizio applicativo e il mapper dei DTO. */
    public CourseController(CourseService courseService, CourseMapper courseMapper) {
        this.courseService = courseService;
        this.courseMapper = courseMapper;
    }

    @PostMapping
    /** Gestisce POST /api/courses e restituisce il corso creato con HTTP 201. */
    public ResponseEntity<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        CourseResponse response = courseMapper.toResponse(courseService.create(request));
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(response.id()).toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{id}")
    /** Gestisce GET /api/courses/{id} e converte l'entity nel DTO di risposta. */
    public CourseResponse getById(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        return courseMapper.toResponse(isTeacher(jwt)
                ? courseService.getByIdForInstructor(id, teacherId(jwt))
                : courseService.getById(id));
    }

    @GetMapping
    /** Gestisce la ricerca paginata dei corsi con testo opzionale. */
    public org.springframework.data.domain.Page<CourseResponse> search(
            @RequestParam(required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by("courseCode").ascending());
        return (isTeacher(jwt) ? courseService.searchByInstructor(teacherId(jwt), query, pageable)
                : courseService.search(query, pageable)).map(courseMapper::toResponse);
    }

    @GetMapping("/status/{status}")
    /** Gestisce il requisito del capitolato per filtrare i corsi per stato. */
    public org.springframework.data.domain.Page<CourseResponse> findByStatus(
            @PathVariable CourseStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @AuthenticationPrincipal Jwt jwt) {
        Pageable pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100),
                Sort.by("courseCode").ascending());
        return (isTeacher(jwt) ? courseService.findByInstructorAndStatus(teacherId(jwt), status, pageable)
                : courseService.findByStatus(status, pageable)).map(courseMapper::toResponse);
    }

    @PutMapping("/{id}")
    /** Gestisce PUT /api/courses/{id} con l'aggiornamento completo del corso. */
    public CourseResponse update(@PathVariable UUID id, @Valid @RequestBody CourseRequest request) {
        return courseMapper.toResponse(courseService.update(id, request));
    }

    @DeleteMapping("/{id}")
    /** Gestisce DELETE /api/courses/{id} e restituisce HTTP 204 se l'eliminazione riesce. */
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        courseService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isTeacher(Jwt jwt) {
        return jwt != null && jwt.getClaimAsStringList("roles") != null
                && jwt.getClaimAsStringList("roles").contains("TEACHER");
    }

    private UUID teacherId(Jwt jwt) {
        return UUID.fromString(jwt.getSubject());
    }
}