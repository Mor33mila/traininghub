package com.traininghub.identity.dto;

import com.traininghub.identity.entity.UserRole;
import jakarta.validation.constraints.NotNull;

public record UserRoleRequest(@NotNull UserRole role) { }