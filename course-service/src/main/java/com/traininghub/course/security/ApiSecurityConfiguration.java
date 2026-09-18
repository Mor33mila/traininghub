package com.traininghub.course.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;

@Configuration
public class ApiSecurityConfiguration {
    @Bean
    /** Costruisce la catena stateless e assegna permessi diversi ai ruoli del token. */
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
            .cors(org.springframework.security.config.Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/courses/**")
                        .hasAnyRole("ADMINISTRATOR", "TUTOR", "TEACHER")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/courses/**")
                        .hasAnyRole("ADMINISTRATOR", "TUTOR")
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/courses/**")
                        .hasAnyRole("ADMINISTRATOR", "TUTOR")
                        .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/courses/**")
                        .hasRole("ADMINISTRATOR")
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())))
                .build();
    }

    /** Converte il claim roles del token in autorita' ROLE_* di Spring Security. */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authorities = new JwtGrantedAuthoritiesConverter();
        authorities.setAuthoritiesClaimName("roles");
        authorities.setAuthorityPrefix("ROLE_");
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authorities);
        return converter;
    }
}