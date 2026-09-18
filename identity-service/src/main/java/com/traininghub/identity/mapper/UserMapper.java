package com.traininghub.identity.mapper;

import com.traininghub.identity.dto.UserRequest;
import com.traininghub.identity.dto.UserResponse;
import com.traininghub.identity.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    /** Crea un'entity utente dalla richiesta profilo validata. */
    public User toEntity(UserRequest request) {
        User user = new User();
        updateEntity(user, request);
        return user;
    }

    /** Copia i campi e normalizza username ed e-mail. */
    public void updateEntity(User user, UserRequest request) {
        user.setUsername(request.getUsername().trim().toLowerCase());
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setRole(request.getRole());
        if (user.getActive() == null) user.setActive(true);
    }

    /** Converte l'entity nel DTO senza esporre credenziali. */
    public UserResponse toResponse(User user) {
        return new UserResponse(user.getId(), user.getUsername(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getRole(), user.getActive());
    }
}