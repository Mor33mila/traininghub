package com.traininghub.course.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class ApiConfiguration {
    @Bean
    Clock applicationClock() {
        return Clock.systemUTC();
    }
}