package com.traininghub.enrollment.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    OpenAPI trainingHubEnrollmentApi() {
        return new OpenAPI().info(new Info()
                .title("TrainingHub Enrollment Service API")
                .version("1.0.0")
                .description("API for managing course enrollments"));
    }
}