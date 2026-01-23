package com.example.keklock.profile.dto;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    String bio,

    String avatarUrl
) {}
