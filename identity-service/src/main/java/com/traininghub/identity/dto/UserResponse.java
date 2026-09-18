package com.traininghub.identity.dto;

import com.traininghub.identity.entity.UserRole;
import java.util.UUID;

public record UserResponse(UUID id, String username, String firstName, String lastName,
                           String email, UserRole role, Boolean active) { }