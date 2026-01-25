package com.example.keklock.profile.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Size(max = 500, message = "Bio must not exceed 500 characters")
    String bio,

    @Pattern(regexp = "^(https?://)?([\\w-]+\\.)+[\\w-]+(/[\\w-./?%&=]*)?$|^$",
             message = "Avatar URL must be a valid URL or empty")
    String avatarUrl
) {}
