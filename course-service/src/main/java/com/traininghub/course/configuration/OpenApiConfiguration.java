package com.traininghub.course.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    OpenAPI trainingHubCourseApi() {
        return new OpenAPI().info(new Info()
                .title("TrainingHub Course Service API")
                .version("1.0.0")
                .description("API for managing TrainingHub courses"));
    }
}