package com.example.keklock.auth.dto;

public record UserRegistrationResponse(
    String identityId,
    String username,
    String email,
    String message
) {}
