package com.traininghub.identity.service;

import com.traininghub.identity.dto.LoginRequest;
import com.traininghub.identity.dto.LoginResponse;
import com.traininghub.identity.entity.User;
import com.traininghub.identity.entity.UserRole;
import com.traininghub.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthServiceTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtEncoder jwtEncoder = mock(JwtEncoder.class);
    private final AuthService service = new AuthService(repository, passwordEncoder, jwtEncoder, "traininghub-identity");

    @Test
    void returnsTokenForValidCredentials() {
        User user = userWithPassword("Password123!");
        when(repository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(user));
        when(jwtEncoder.encode(any(JwtEncoderParameters.class))).thenReturn(
            Jwt.withTokenValue("test-token").header("alg", "HS256")
                .claim("sub", user.getId().toString()).build());

        LoginResponse response = service.login(new LoginRequest("admin", "Password123!"));

        assertEquals("test-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    void rejectsInvalidCredentials() {
        User user = userWithPassword("Password123!");
        when(repository.findByUsernameIgnoreCase("admin")).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class,
                () -> service.login(new LoginRequest("admin", "wrong-password")));
    }

    private User userWithPassword(String password) {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("admin");
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(UserRole.ADMINISTRATOR);
        user.setActive(true);
        return user;
    }
}