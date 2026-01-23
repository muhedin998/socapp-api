package com.example.keklock.profile.dto;

import com.example.keklock.profile.domain.Profile;

import java.time.LocalDateTime;

public record ProfileResponse(
    Long id,
    String identityId,
    String username,
    String email,
    String firstName,
    String lastName,
    String bio,
    String avatarUrl,
    int followersCount,
    int followingCount,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static ProfileResponse from(Profile profile) {
        return new ProfileResponse(
            profile.getId(),
            profile.getIdentityId(),
            profile.getUsername(),
            profile.getEmail(),
            profile.getFirstName(),
            profile.getLastName(),
            profile.getBio(),
            profile.getAvatarUrl(),
            profile.getFollowersCount(),
            profile.getFollowingCount(),
            profile.getCreatedAt(),
            profile.getUpdatedAt()
        );
    }
}
