package com.traininghub.identity.service;

import com.traininghub.identity.dto.UserRequest;
import com.traininghub.identity.entity.User;
import com.traininghub.identity.entity.UserRole;
import com.traininghub.identity.exception.DuplicateUserException;
import com.traininghub.identity.mapper.UserMapper;
import com.traininghub.identity.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UserServiceTest {
    private final UserRepository repository = mock(UserRepository.class);
    private final UserService service = new UserService(repository, new UserMapper(), new BCryptPasswordEncoder());

    @Test
    void rejectsDuplicateUsername() {
        UserRequest request = validRequest();
        when(repository.existsByUsernameIgnoreCase("mario.rossi")).thenReturn(true);
        assertThrows(DuplicateUserException.class, () -> service.create(request));
    }

    @Test
    void createsUserWithNormalizedValues() {
        UserRequest request = validRequest();
        when(repository.existsByUsernameIgnoreCase("mario.rossi")).thenReturn(false);
        when(repository.existsByEmailIgnoreCase("mario@example.com")).thenReturn(false);
        when(repository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        User user = service.create(request);
        assertEquals("mario.rossi", user.getUsername());
        assertEquals("mario@example.com", user.getEmail());
        assertEquals(true, user.getActive());
    }

    private UserRequest validRequest() {
        UserRequest request = new UserRequest();
        request.setUsername("MARIO.ROSSI");
        request.setFirstName("Mario");
        request.setLastName("Rossi");
        request.setEmail("MARIO@EXAMPLE.COM");
        request.setPassword("Password123!");
        request.setRole(UserRole.TUTOR);
        return request;
    }
}