package com.traininghub.enrollment.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.UUID;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class RestCourseClient implements CourseClient {
    private final RestClient client;

    /** Costruisce un client REST usando URL esterno e credenziali Basic opzionali. */
    public RestCourseClient(RestClient.Builder builder,
                            @Value("${course-service.base-url}") String baseUrl,
                            @Value("${course-service.username}") String username,
                            @Value("${course-service.password}") String password) {
        this.client = builder.baseUrl(baseUrl).defaultHeaders(headers -> {
            if (!username.isBlank()) headers.setBasicAuth(username, password);
        }).requestInterceptor((request, body, execution) -> {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                String authorization = attributes.getRequest().getHeader("Authorization");
                if (authorization != null && !authorization.isBlank()) request.getHeaders().set("Authorization", authorization);
            }
            return execution.execute(request, body);
        }).build();
    }

    @Override
    /** Recupera il riepilogo corso necessario per capienza e validazione presenze. */
    public CourseSummary findById(UUID courseId) {
        return client.get().uri("/api/courses/{id}", courseId)
                .retrieve().body(CourseSummary.class);
    }
}