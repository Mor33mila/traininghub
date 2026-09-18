package com.traininghub.identity.dto;

public record LoginResponse(String accessToken, String tokenType, long expiresInSeconds) { }