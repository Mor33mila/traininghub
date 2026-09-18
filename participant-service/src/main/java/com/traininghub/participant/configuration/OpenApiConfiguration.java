package com.traininghub.participant.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    OpenAPI trainingHubParticipantApi() {
        return new OpenAPI().info(new Info()
                .title("TrainingHub Participant Service API")
                .version("1.0.0")
                .description("API for managing TrainingHub participants"));
    }
}