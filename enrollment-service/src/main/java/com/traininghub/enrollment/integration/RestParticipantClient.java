package com.traininghub.enrollment.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import java.util.UUID;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class RestParticipantClient implements ParticipantClient {
    private final RestClient client;

    /** Costruisce un client REST usando URL esterno e credenziali Basic opzionali. */
    public RestParticipantClient(RestClient.Builder builder,
                                 @Value("${participant-service.base-url}") String baseUrl,
                                 @Value("${participant-service.username}") String username,
                                 @Value("${participant-service.password}") String password) {
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
    /** Recupera lo stato del partecipante prima di creare un'iscrizione. */
    public ParticipantSummary findById(UUID participantId) {
        return client.get().uri("/api/participants/{id}", participantId)
                .retrieve().body(ParticipantSummary.class);
    }
}