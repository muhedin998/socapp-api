package com.example.keklock.profile.event;

public record ProfileCreatedEvent(
    Long profileId,
    String identityId,
    String username,
    String email
) {
}
